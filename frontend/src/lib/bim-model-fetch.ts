/**
 * BIM 3D モデル取得（認証付き API パスは Blob URL に変換）。
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

function isGlbBytes(bytes: Uint8Array): boolean {
  return (
    bytes.length >= 4 &&
    bytes[0] === 0x67 &&
    bytes[1] === 0x6c &&
    bytes[2] === 0x54 &&
    bytes[3] === 0x46
  )
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
  return {
    buffer: await res.arrayBuffer(),
    contentType: res.headers.get('content-type'),
  }
}

function assertModelBytes(buffer: ArrayBuffer, sourceUrl: string) {
  if (buffer.byteLength === 0) {
    throw new Error('モデルファイルが空です。GLB 形式で再アップロードしてください')
  }
  const bytes = new Uint8Array(buffer)
  const lower = sourceUrl.toLowerCase()
  if (lower.endsWith('.glb') && !isGlbBytes(bytes)) {
    throw new Error('GLB ファイルの形式が不正です。別の GLB ファイルをお試しください')
  }
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

    const resourceUrl = new URL(uri, baseUrl).toString()
    try {
      const { buffer: resourceBytes } = await fetchAuthedBytes(resourceUrl)
      const encoded = new Uint8Array(resourceBytes)
      buffer.uri = `data:application/octet-stream;base64,${bytesToBase64(encoded)}`
    } catch {
      throw new Error(
        'GLTF に外部ファイル参照があります。関連 .bin が見つかりません。GLB 形式（単一ファイル）で再アップロードしてください',
      )
    }
  }
  return next
}

async function resolveUploadedModelBlob(sourceUrl: string): Promise<Blob> {
  const { buffer, contentType } = await fetchAuthedBytes(sourceUrl)
  assertModelBytes(buffer, sourceUrl)

  const lower = sourceUrl.toLowerCase()
  if (lower.endsWith('.gltf')) {
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

  const mime = inferModelMime(sourceUrl, contentType)
  return new Blob([buffer], { type: mime })
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

  const blob = await resolveUploadedModelBlob(trimmed)
  const objectUrl = URL.createObjectURL(blob)
  return {
    url: objectUrl,
    revoke: () => URL.revokeObjectURL(objectUrl),
  }
}
