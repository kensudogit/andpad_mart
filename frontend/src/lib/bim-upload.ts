/**
 * BIM サムネイル画像アップロード（multipart → バックエンド API）。
 */
import { getAuthToken } from '@/lib/auth-session'

export type BimThumbnailUploadResult = {
  url: string
  fileName?: string
  contentType?: string
  sizeBytes?: number
  bimModelId?: string
}

export async function uploadBimThumbnail(
  file: File,
  bimModelId?: string,
): Promise<BimThumbnailUploadResult> {
  const form = new FormData()
  form.append('file', file)
  if (bimModelId) {
    form.append('bimModelId', bimModelId)
  }

  const headers: HeadersInit = {}
  const token = getAuthToken()
  if (token) {
    headers.Authorization = `Bearer ${token}`
  }

  const res = await fetch('/api/saas/bim/upload', {
    method: 'POST',
    body: form,
    credentials: 'include',
    headers,
  })

  if (!res.ok) {
    let message = `upload failed (${res.status})`
    try {
      const body = (await res.json()) as { error?: string; message?: string }
      message = body.error || body.message || message
    } catch {
      // ignore parse errors
    }
    throw new Error(message)
  }

  return (await res.json()) as BimThumbnailUploadResult
}

export function isUploadedBimThumbnail(url?: string | null) {
  return Boolean(url && url.includes('/api/saas/bim/files/'))
}

export function resolveBimThumbnailSrc(url?: string | null) {
  if (!url) return null
  if (url.startsWith('/')) {
    return url
  }
  return url
}
