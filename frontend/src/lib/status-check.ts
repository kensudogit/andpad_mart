/**
 * サーバー側 API 接続診断（/status ページ・/api/status 共通）。
 */
import { isRailway, isUnifiedDeploy, resolveApiUrl } from '@/lib/resolve-api-url'

export type SetupStatus = {
  postgres?: boolean
  databaseSource?: string
  databaseUrl?: string
  databasePrivateUrl?: string
  pgHost?: string
  jwtSecret?: string
  jwtSecretWarning?: string
  openaiApiKey?: string
  railway?: boolean
  publicUrl?: string
  hint?: string
}

export type StatusPayload = {
  service: string
  ok: boolean
  apiUrl: string
  apiUrlNote?: string
  graphqlProxy: string
  unified: boolean
  postgres?: boolean
  openai?: boolean
  setup?: SetupStatus
  health: { ok?: boolean; service?: string; version?: string }
  error?: string
}

function envPresence(key: string): string {
  const raw = process.env[key]?.trim()
  if (!raw) return 'empty'
  if (raw.includes('${{')) return 'unresolved'
  return 'set'
}

function jwtSecretWarning(): string | undefined {
  const jwt = process.env.JWT_SECRET?.trim()
  if (!jwt) return 'JWT_SECRET is empty'
  if (jwt.startsWith('sk-ant') || jwt.startsWith('sk-proj') || jwt.startsWith('sk-')) {
    return 'JWT_SECRET looks like an API key. Use a random string; put OpenAI keys in OPENAI_API_KEY.'
  }
  if (jwt === 'dev-only-change-in-production-min-32-chars') {
    return 'JWT_SECRET is still the dev default. Set a long random string on Railway.'
  }
  return undefined
}

function dbConfigured(): boolean {
  for (const key of ['DATABASE_URL', 'DATABASE_PRIVATE_URL', 'PGHOST'] as const) {
    const v = process.env[key]?.trim()
    if (v && !v.includes('${{')) return true
  }
  return false
}

/** Java API が落ちているとき、Next.js プロセスから見える環境変数だけで診断 */
function localSetupWhenApiDown(): SetupStatus {
  const databaseUrl = envPresence('DATABASE_URL')
  const databasePrivateUrl = envPresence('DATABASE_PRIVATE_URL')
  const pgHost = envPresence('PGHOST')
  const jwtSecret = envPresence('JWT_SECRET')
  const openaiApiKey = envPresence('OPENAI_API_KEY')
  const railway = isRailway()
  const jwtSecretWarningMsg = jwtSecretWarning()

  let hint: string | undefined
  if (!dbConfigured()) {
    hint =
      'DATABASE_URL is not configured — Java API will not start. ' +
      'andpad_j service → Variables → Reference → Postgres → DATABASE_URL, then Redeploy.'
  } else if (databaseUrl === 'unresolved' || databasePrivateUrl === 'unresolved') {
    hint = 'DATABASE_URL reference is unresolved (${{...}}). Fix the variable reference and Redeploy.'
  } else if (jwtSecret === 'empty') {
    hint = 'Set JWT_SECRET (32+ random chars) on the app service, then Redeploy.'
  } else {
    hint =
      'Java API is not responding on 127.0.0.1:8081. Check Railway Deploy logs for ' +
      '"[unified] Java API ready" or "[unified] ERROR".'
  }

  return {
    databaseUrl,
    databasePrivateUrl,
    pgHost,
    jwtSecret,
    jwtSecretWarning: jwtSecretWarningMsg,
    openaiApiKey,
    railway,
    databaseSource: dbConfigured()
      ? envPresence('DATABASE_URL') !== 'empty'
        ? 'DATABASE_URL'
        : envPresence('DATABASE_PRIVATE_URL') !== 'empty'
          ? 'DATABASE_PRIVATE_URL'
          : 'PGHOST'
      : 'none',
    publicUrl: process.env.RAILWAY_PUBLIC_DOMAIN?.trim()
      ? `https://${process.env.RAILWAY_PUBLIC_DOMAIN.trim()}`
      : undefined,
    hint,
  }
}

/** Go API の接続状態と環境診断を取得（/status・/api/status 共通） */
export async function fetchApiStatus(): Promise<StatusPayload> {
  const apiUrl = resolveApiUrl()
  const unified = isUnifiedDeploy()

  let health: StatusPayload['health'] = {}
  let apiReachable = false
  let postgres: boolean | undefined
  let openai: boolean | undefined
  let setup: SetupStatus | undefined
  let error: string | undefined

  try {
    const res = await fetch(`${apiUrl}/health`, { cache: 'no-store' })
    health = (await res.json()) as StatusPayload['health']
    apiReachable = res.ok && health.ok === true

    const statusRes = await fetch(`${apiUrl}/status`, { cache: 'no-store' })
    if (statusRes.ok) {
      const statusJson = (await statusRes.json()) as {
        postgres?: boolean
        openai?: boolean
        setup?: SetupStatus
      }
      postgres = statusJson.postgres
      openai = statusJson.openai
      setup = statusJson.setup
    }
  } catch (e) {
    error = e instanceof Error ? e.message : String(e)
    if (unified) {
      setup = localSetupWhenApiDown()
    }
  }

  const payload: StatusPayload = {
    service: 'andpad-web',
    ok: apiReachable,
    apiUrl,
    graphqlProxy: '/graphql',
    unified,
    postgres,
    openai,
    setup,
    health,
    error,
  }

  // 統合デプロイでは 127.0.0.1 は正常（ブラウザは /graphql プロキシを使用）
  if (unified && apiUrl.includes('127.0.0.1')) {
    payload.apiUrlNote =
      'Unified deploy: API runs inside the same container. Browsers use /graphql and /auth on this site (not 127.0.0.1).'
  }

  if (!apiReachable && setup?.hint) {
    payload.apiUrlNote =
      (payload.apiUrlNote ? payload.apiUrlNote + ' ' : '') + setup.hint
  }

  if (apiReachable && postgres === false) {
    payload.apiUrlNote =
      (payload.apiUrlNote ? payload.apiUrlNote + ' ' : '') +
      (setup?.hint ??
        'PostgreSQL is not connected. Set DATABASE_URL (Postgres Reference) on the Railway app service.')
  }

  return payload
}
