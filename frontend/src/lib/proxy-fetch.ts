/**
 * サーバー側プロキシ用 fetch（タイムアウト付き）。
 * auth / graphql ルートから Java API へ転送する際に使用。
 */
import { inferApiStartupHint, readApiStartupLogTail } from '@/lib/api-startup-log'
import { isRailway, listApiBaseCandidates, unifiedDeployActive } from '@/lib/resolve-api-url'
import { dbConfigured } from '@/lib/status-check'

export const PROXY_TIMEOUT_DEFAULT_MS = 15_000
/** OpenAI 連携 mutation（チャット・RAG・AI Board）向け */
export const PROXY_TIMEOUT_GRAPHQL_POST_MS = 120_000
export const PROXY_TIMEOUT_AUTH_MS = 20_000
/** BIM モデル（GLB）アップロード向け */
export const PROXY_TIMEOUT_BIM_UPLOAD_MS = 120_000

function forwardAuthHeadersFromRequest(request: Request, headers: Headers) {
  const cookie = request.headers.get('cookie')
  if (cookie) headers.set('cookie', cookie)
  const authorization = request.headers.get('authorization')
  if (authorization) headers.set('authorization', authorization)
}

/** multipart を再パースせず生ボディのまま Java API へ転送 */
export async function proxyMultipartToApi(
  request: Request,
  apiPath: string,
  timeoutMs = PROXY_TIMEOUT_BIM_UPLOAD_MS,
): Promise<Response> {
  const bases = listApiBaseCandidates()
  const contentType = request.headers.get('content-type')
  if (!contentType || !contentType.toLowerCase().includes('multipart/form-data')) {
    return Response.json({ error: 'multipart/form-data is required' }, { status: 400 })
  }

  const body = Buffer.from(await request.arrayBuffer())
  const failures: string[] = []

  for (const base of bases) {
    const target = `${base}${apiPath}`
    const headers = new Headers()
    headers.set('content-type', contentType)
    forwardAuthHeadersFromRequest(request, headers)

    try {
      const upstream = await fetchUpstream(
        target,
        { method: 'POST', headers, body },
        timeoutMs,
      )
      const text = await upstream.text()
      const outHeaders = new Headers()
      const upstreamType = upstream.headers.get('content-type')
      if (upstreamType) outHeaders.set('content-type', upstreamType)
      return new Response(text, { status: upstream.status, headers: outHeaders })
    } catch (err) {
      failures.push(`${base}: ${err instanceof Error ? err.message : String(err)}`)
    }
  }

  const hint = proxyConnectionHint()
  return Response.json(
    {
      error: `Cannot reach API (${failures.join('; ')})`,
      hint,
    },
    { status: 502 },
  )
}

/** 上流 API へタイムアウト付き fetch */
export async function fetchUpstream(
  url: string,
  init: RequestInit = {},
  timeoutMs = PROXY_TIMEOUT_DEFAULT_MS,
): Promise<Response> {
  return fetch(url, {
    ...init,
    signal: AbortSignal.timeout(timeoutMs),
  })
}

/** ログイン応答の Set-Cookie をブラウザへ中継 */
function forwardSetCookies(upstream: Response, outHeaders: Headers) {
  const setCookies =
    typeof upstream.headers.getSetCookie === 'function'
      ? upstream.headers.getSetCookie()
      : upstream.headers.get('set-cookie')
        ? [upstream.headers.get('set-cookie')!]
        : []
  for (const value of setCookies) {
    outHeaders.append('set-cookie', value)
  }
}

/** 最初に応答した上流をそのまま返す（401 等のアプリエラーも含む） */
export async function proxyUpstreamResponse(upstream: Response): Promise<Response> {
  const text = await upstream.text()
  const outHeaders = new Headers()
  const upstreamType = upstream.headers.get('content-type')
  if (upstreamType) outHeaders.set('content-type', upstreamType)
  forwardSetCookies(upstream, outHeaders)
  return new Response(text, { status: upstream.status, headers: outHeaders })
}

/** 統合デプロイで API 未到達時の診断メッセージ */
function proxyConnectionHint(): string | undefined {
  if (!unifiedDeployActive()) return undefined

  const status = process.env.UNIFIED_API_STATUS?.trim()
  if (status === 'missing_database_url' || !dbConfigured()) {
    return (
      'DATABASE_URL が未設定のため Java API が起動していません。' +
      ' Railway → andpad_mart サービス → Variables → Reference → Postgres → DATABASE_URL を追加して Redeploy してください。'
    )
  }
  if (status === 'unresolved_database_url') {
    return 'DATABASE_URL の参照が未解決です（${{...}}）。変数参照を修正して Redeploy してください。'
  }
  if (status === 'api_exited') {
    const logHint = inferApiStartupHint(readApiStartupLogTail(30))
    return (
      logHint ??
      'Java API が起動中に終了しました。/status の起動ログまたは Railway Deploy ログで [unified] ERROR を確認してください。'
    )
  }
  if (isRailway()) {
    return (
      'Java API (127.0.0.1:8081) に接続できません。' +
      ' /status で DATABASE_URL · JWT_SECRET を確認し、Deploy ログで [unified] Java API ready を探してください。'
    )
  }
  return undefined
}

/** 候補ベース URL を順に試し、接続失敗時のみ次へ */
export async function proxyToApiBases(
  bases: string[],
  buildTarget: (base: string) => string,
  init: RequestInit,
  timeoutMs = PROXY_TIMEOUT_DEFAULT_MS,
): Promise<Response> {
  const failures: string[] = []
  for (const base of bases) {
    const target = buildTarget(base)
    try {
      const upstream = await fetchUpstream(target, init, timeoutMs)
      return proxyUpstreamResponse(upstream)
    } catch (err) {
      failures.push(`${base}: ${err instanceof Error ? err.message : String(err)}`)
    }
  }
  const hint = proxyConnectionHint()
  return Response.json(
    {
      error: `Cannot reach API (${failures.join('; ')})`,
      hint,
    },
    { status: 502 },
  )
}
