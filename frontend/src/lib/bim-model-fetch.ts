/**
 * BIM 3D モデル取得（認証付き API パスは Blob URL に変換）。
 */
import { isUploadedBimAsset } from '@/lib/bim-assets'
import { getAuthToken } from '@/lib/auth-session'

function inferModelMime(url: string, header: string | null): string {
  const fromHeader = header?.split(';')[0]?.trim()
  if (fromHeader && (fromHeader.includes('gltf') || fromHeader === 'model/gltf-binary')) {
    return fromHeader
  }
  const lower = url.toLowerCase()
  if (lower.endsWith('.glb')) return 'model/gltf-binary'
  if (lower.endsWith('.gltf')) return 'model/gltf+json'
  return 'model/gltf-binary'
}

export type ResolvedBimModel = {
  url: string
  revoke: () => void
}

/** model-viewer 用の src（API 配信分は認証付き fetch → Blob URL） */
export async function resolveBimModelObjectUrl(sourceUrl: string): Promise<ResolvedBimModel> {
  const trimmed = sourceUrl.trim()
  if (!trimmed) {
    throw new Error('モデル URL が空です')
  }

  if (!isUploadedBimAsset(trimmed)) {
    return { url: trimmed, revoke: () => {} }
  }

  const headers: HeadersInit = {}
  const token = getAuthToken()
  if (token) {
    headers.Authorization = `Bearer ${token}`
  }

  const res = await fetch(trimmed, { credentials: 'include', headers })
  if (!res.ok) {
    throw new Error(`モデルの読み込みに失敗しました (${res.status})`)
  }

  const buffer = await res.arrayBuffer()
  const mime = inferModelMime(trimmed, res.headers.get('content-type'))
  const blob = new Blob([buffer], { type: mime })
  const objectUrl = URL.createObjectURL(blob)
  return {
    url: objectUrl,
    revoke: () => URL.revokeObjectURL(objectUrl),
  }
}
