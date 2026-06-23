/**
 * BIM ファイルアップロード（サムネイル / 3D モデル）。
 */
import { getAuthToken } from '@/lib/auth-session'

export type BimUploadResult = {
  url: string
  fileName?: string
  contentType?: string
  sizeBytes?: number
  fileSizeMb?: number
  bimModelId?: string
}

async function uploadBimFile(
  endpoint: '/api/saas/bim/upload' | '/api/saas/bim/upload/model',
  file: File,
  bimModelId?: string,
): Promise<BimUploadResult> {
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

  const res = await fetch(endpoint, {
    method: 'POST',
    body: form,
    credentials: 'include',
    headers,
  })

  if (!res.ok) {
    let message = `upload failed (${res.status})`
    const raw = await res.text()
    if (raw.trim()) {
      try {
        const body = JSON.parse(raw) as {
          error?: string
          message?: string
          path?: string
        }
        const detail = body.error || body.message
        if (detail && detail !== 'Internal Server Error') {
          message = detail
        } else if (body.path) {
          message = `${detail ?? 'upload failed'} (${body.path})`
        } else if (detail) {
          message = detail
        }
      } catch {
        message = raw.trim().slice(0, 300)
      }
    }
    throw new Error(message)
  }

  return (await res.json()) as BimUploadResult
}

export async function uploadBimThumbnail(file: File, bimModelId?: string) {
  return uploadBimFile('/api/saas/bim/upload', file, bimModelId)
}

export async function uploadBimModel(file: File, bimModelId?: string) {
  return uploadBimFile('/api/saas/bim/upload/model', file, bimModelId)
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
