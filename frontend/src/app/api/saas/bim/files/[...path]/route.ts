/**
 * アップロード済み BIM 画像ファイルをバックエンドから配信。
 */
import { listApiBaseCandidates } from '@/lib/resolve-api-url'
import { fetchUpstream } from '@/lib/proxy-fetch'

export const runtime = 'nodejs'
export const dynamic = 'force-dynamic'

function forwardAuthHeaders(request: Request, headers: Headers) {
  const cookie = request.headers.get('cookie')
  if (cookie) headers.set('cookie', cookie)
  const authorization = request.headers.get('authorization')
  if (authorization) headers.set('authorization', authorization)
  const accessToken = new URL(request.url).searchParams.get('access_token')
  if (accessToken && !headers.has('authorization')) {
    headers.set('authorization', `Bearer ${accessToken}`)
  }
}

type Ctx = { params: Promise<{ path: string[] }> }

/** アップロード済み BIM 画像を取得 */
export async function GET(request: Request, ctx: Ctx) {
  const { path } = await ctx.params
  const bases = listApiBaseCandidates()
  const search = new URL(request.url).search
  const headers = new Headers()
  forwardAuthHeaders(request, headers)
  const relativePath = path.join('/')

  const failures: string[] = []
  for (const base of bases) {
    const target = `${base}/api/saas/bim/files/${relativePath}${search}`
    try {
      const upstream = await fetchUpstream(target, { method: 'GET', headers })
      const buffer = await upstream.arrayBuffer()
      const outHeaders = new Headers()
      const contentType = upstream.headers.get('content-type')
      if (contentType) outHeaders.set('content-type', contentType)
      outHeaders.set('cache-control', 'private, max-age=3600')
      return new Response(buffer, { status: upstream.status, headers: outHeaders })
    } catch (err) {
      failures.push(`${base}: ${err instanceof Error ? err.message : String(err)}`)
    }
  }

  return Response.json({ error: `Cannot reach API (${failures.join('; ')})` }, { status: 502 })
}
