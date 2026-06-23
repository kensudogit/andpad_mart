/**
 * BIM 3D モデル（GLB/GLTF）multipart アップロードをバックエンドへ転送。
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
}

/** 3D モデルファイルをアップロード */
export async function POST(request: Request): Promise<Response> {
  const bases = listApiBaseCandidates()
  const formData = await request.formData()
  const headers = new Headers()
  forwardAuthHeaders(request, headers)

  const failures: string[] = []
  for (const base of bases) {
    const target = `${base}/api/saas/bim/upload/model`
    try {
      const upstream = await fetchUpstream(
        target,
        { method: 'POST', headers, body: formData },
        120_000,
      )
      const text = await upstream.text()
      const outHeaders = new Headers()
      const contentType = upstream.headers.get('content-type')
      if (contentType) outHeaders.set('content-type', contentType)
      return new Response(text, { status: upstream.status, headers: outHeaders })
    } catch (err) {
      failures.push(`${base}: ${err instanceof Error ? err.message : String(err)}`)
    }
  }

  return Response.json({ error: `Cannot reach API (${failures.join('; ')})` }, { status: 502 })
}

export async function OPTIONS() {
  return new Response(null, {
    status: 204,
    headers: {
      'access-control-allow-origin': '*',
      'access-control-allow-methods': 'POST, OPTIONS',
      'access-control-allow-headers': 'content-type, authorization, cookie',
    },
  })
}
