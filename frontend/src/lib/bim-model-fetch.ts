/**
 * BIM 3D モデル取得（認証付き API パス対応）。
 */
import { isUploadedBimAsset } from '@/lib/bim-assets'
import { getAuthToken } from '@/lib/auth-session'

type GltfDocument = {
  buffers?: Array<{ byteLength?: number; uri?: string }>
  [key: string]: unknown
}

function authHeaders(): HeadersInit {
  const headers: HeadersInit = {}
  const token = getAuthToken()
  if (token) {
    headers.Authorization = `Bearer ${token}`
  }
  return headers
}

export function buildAuthedModelUrl(sourceUrl: string): string {
  if (typeof window === 'undefined') return sourceUrl
  const token = getAuthToken()
  if (!token || !isUploadedBimAsset(sourceUrl)) return sourceUrl
  const url = new URL(sourceUrl, window.location.origin)
  url.searchParams.set('access_token', token)
  return `${url.pathname}${url.search}`
}

function isGlbBytes(bytes: Uint8Array): boolean {
  return (
    bytes.length >= 4 &&
    bytes[0] === 0x67 &&
    bytes[1] === 0x6c &&
    bytes[2] === 0x54 &&
    bytes[3] === 0x46
  )
}

function looksLikeGltfJson(bytes: Uint8Array): boolean {
  for (let i = 0; i < Math.min(bytes.length, 256); i++) {
    const ch = bytes[i]
    if (ch === 0x20 || ch === 0x09 || ch === 0x0a || ch === 0x0d) continue
    return ch === 0x7b
  }
  return false
}

function bytesToBase64(bytes: Uint8Array): string {
  const chunk = 0x8000
  let binary = ''
  for (let i = 0; i < bytes.length; i += chunk) {
    binary += String.fromCharCode(...bytes.subarray(i, i + chunk))
  }
  return btoa(binary)
}

async function fetchAuthedBytes(url: string): Promise<{ buffer: ArrayBuffer; contentType: string | null }> {
  const res = await fetch(url, { credentials: 'include', headers: authHeaders() })
  if (!res.ok) {
    throw new Error(`モデルの読み込みに失敗しました (${res.status})`)
  }
  const buffer = await res.arrayBuffer()
  const contentType = res.headers.get('content-type')
  if (contentType?.includes('text/html')) {
    throw new Error('モデルファイルの取得に失敗しました。ログインし直してからお試しください')
  }
  return { buffer, contentType }
}

function assertRecognizedModel(buffer: ArrayBuffer) {
  if (buffer.byteLength === 0) {
    throw new Error('モデルファイルが空です。GLB 形式で再アップロードしてください')
  }
  const bytes = new Uint8Array(buffer)
  if (isGlbBytes(bytes) || looksLikeGltfJson(bytes)) return

  const preview = new TextDecoder().decode(bytes.slice(0, 120)).trim()
  if (preview.startsWith('<') || preview.startsWith('<!')) {
    throw new Error('モデルではなく HTML が返されました。再アップロードしてください')
  }
  if (preview.startsWith('{"error"')) {
    throw new Error('モデルファイルを取得できませんでした。再アップロードしてください')
  }
  throw new Error(
    `3Dモデル形式を認識できません（${buffer.byteLength} バイト）。単一ファイルの GLB で再アップロードしてください`,
  )
}

async function embedGltfExternalBuffers(
  gltf: GltfDocument,
  baseUrl: string,
): Promise<GltfDocument> {
  const buffers = gltf.buffers
  if (!buffers?.length) return gltf

  const next = structuredClone(gltf) as GltfDocument
  for (const buffer of next.buffers ?? []) {
    const uri = buffer.uri?.trim()
    if (!uri || uri.startsWith('data:')) continue

    const resourceUrl = buildAuthedModelUrl(new URL(uri, baseUrl).toString())
    try {
      const { buffer: resourceBytes } = await fetchAuthedBytes(resourceUrl)
      const encoded = new Uint8Array(resourceBytes)
      buffer.uri = `data:application/octet-stream;base64,${bytesToBase64(encoded)}`
    } catch {
      throw new Error(
        'GLTF に外部 .bin 参照があります。単一ファイルの GLB 形式で再アップロードしてください',
      )
    }
  }
  return next
}

async function resolveUploadedGltfBlob(sourceUrl: string, buffer: ArrayBuffer): Promise<Blob> {
  const text = new TextDecoder().decode(buffer)
  let gltf: GltfDocument
  try {
    gltf = JSON.parse(text) as GltfDocument
  } catch {
    throw new Error('GLTF JSON の解析に失敗しました。GLB 形式で再アップロードしてください')
  }
  const embedded = await embedGltfExternalBuffers(gltf, sourceUrl)
  return new Blob([JSON.stringify(embedded)], { type: 'model/gltf+json' })
}

export type ResolvedBimModel = {
  url: string
  revoke: () => void
}

/** model-viewer 用 src（API 配信 GLB は認証付き直接 URL、GLTF は Blob） */
export async function resolveBimModelObjectUrl(sourceUrl: string): Promise<ResolvedBimModel> {
  const trimmed = sourceUrl.trim()
  if (!trimmed) {
    throw new Error('モデル URL が空です')
  }

  if (!isUploadedBimAsset(trimmed)) {
    return { url: trimmed, revoke: () => {} }
  }

  const { buffer } = await fetchAuthedBytes(trimmed)
  assertRecognizedModel(buffer)
  const bytes = new Uint8Array(buffer)

  if (isGlbBytes(bytes)) {
    if (trimmed.toLowerCase().endsWith('.glb')) {
      return { url: buildAuthedModelUrl(trimmed), revoke: () => {} }
    }
    const objectUrl = URL.createObjectURL(new Blob([buffer], { type: 'model/gltf-binary' }))
    return {
      url: objectUrl,
      revoke: () => URL.revokeObjectURL(objectUrl),
    }
  }

  const blob = await resolveUploadedGltfBlob(trimmed, buffer)
  const objectUrl = URL.createObjectURL(blob)
  return {
    url: objectUrl,
    revoke: () => URL.revokeObjectURL(objectUrl),
  }
}
