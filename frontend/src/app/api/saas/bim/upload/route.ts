/**
 * BIM サムネイル multipart アップロードをバックエンドへ転送（生ボディ）。
 */
import { PROXY_TIMEOUT_BIM_UPLOAD_MS, proxyMultipartToApi } from '@/lib/proxy-fetch'

export const runtime = 'nodejs'
export const dynamic = 'force-dynamic'
export const maxDuration = 120

/** サムネイル画像をアップロード */
export async function POST(request: Request): Promise<Response> {
  try {
    return await proxyMultipartToApi(
      request,
      '/api/saas/bim/upload/thumbnail',
      PROXY_TIMEOUT_BIM_UPLOAD_MS,
    )
  } catch (err) {
    return Response.json(
      { error: err instanceof Error ? err.message : 'thumbnail upload failed' },
      { status: 500 },
    )
  }
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
