/** BIM サムネイル種別（DB / タイトル / フォーマットから解決）。 */
export type BimThumbKind = 'default' | 'structure' | 'equipment' | 'renovation'

/** 自前ホストの glTF サンプル（CDN 403 回避・オフライン用） */
export const BIM_SAMPLE_MODEL_LOCAL_GLTF = '/bim/samples/andpad-sample.gltf'
export const BIM_SAMPLE_MODEL_LOCAL_GLTF_EMBEDDED = '/bim/samples/andpad-sample-embedded.gltf'
export const BIM_SAMPLE_MODEL_LOCAL_GLB = '/bim/samples/andpad-sample.glb'

/** モデルとして扱う最小サイズ（バイト） */
export const BIM_MIN_MODEL_BYTES = 500

const EXTERNAL_VIEWER_HOSTS = ['cdn.jsdelivr.net', 'modelviewer.dev', 'khronosgroup']

export function isBrokenExternalViewerUrl(url?: string | null): boolean {
  const lower = (url ?? '').trim().toLowerCase()
  if (!lower.startsWith('http')) return false
  return EXTERNAL_VIEWER_HOSTS.some((host) => lower.includes(host))
}

export function remapExternalBimViewerUrl(url: string): string {
  const trimmed = url.trim()
  if (!trimmed) return BIM_SAMPLE_MODEL_LOCAL_GLB
  if (isBrokenExternalViewerUrl(trimmed)) return BIM_SAMPLE_MODEL_LOCAL_GLB
  return trimmed
}

export function getBimThumbKind(
  title?: string | null,
  format?: string | null,
  thumbnailUrl?: string | null,
): BimThumbKind {
  const t = title ?? ''
  const u = (thumbnailUrl ?? '').toLowerCase()
  if (t.includes('設備') || u.includes('equipment')) return 'equipment'
  if (t.includes('本館') || t.includes('構造') || u.includes('structure')) return 'structure'
  if (t.includes('改修') || u.includes('renovation')) return 'renovation'
  const fmt = (format ?? '').toLowerCase()
  if (fmt === 'ifc' || fmt === 'revit') return 'equipment'
  if (fmt.includes('gltf') || fmt === 'glb') return 'structure'
  return 'default'
}

export function canUseModelViewer(format?: string | null, viewerUrl?: string | null) {
  const resolved = resolveBimViewerUrl(format, viewerUrl)
  const url = resolved.toLowerCase()
  if (isUploadedBimAsset(url) || url.endsWith('.glb') || url.endsWith('.gltf')) return true
  const fmt = (format ?? '').toLowerCase()
  return fmt.includes('gltf') || fmt === 'glb'
}

export function resolveBimViewerUrl(format?: string | null, viewerUrl?: string | null) {
  const url = remapExternalBimViewerUrl((viewerUrl ?? '').trim())
  if (url.includes('/api/saas/bim/files/')) {
    return url
  }
  if (url && (url.endsWith('.glb') || url.endsWith('.gltf'))) {
    return url
  }
  if (url.startsWith('/bim/samples/')) {
    return url
  }
  const fmt = (format ?? '').toLowerCase()
  if (fmt.includes('gltf') || fmt === 'glb') {
    return BIM_SAMPLE_MODEL_LOCAL_GLB
  }
  return url
}

export function isUploadedBimAsset(url?: string | null) {
  return Boolean(url && url.includes('/api/saas/bim/files/'))
}

export function isEmbeddableViewerPage(viewerUrl?: string | null) {
  const url = (viewerUrl ?? '').trim().toLowerCase()
  if (!url.startsWith('http')) return false
  return !url.endsWith('.glb') && !url.endsWith('.gltf')
}

export function defaultThumbnailPath(kind: BimThumbKind) {
  return `/bim/thumbs/${kind === 'default' ? 'default' : kind}.svg`
}

export function defaultThumbnailForFormat(format: string) {
  const fmt = format.toLowerCase()
  if (fmt === 'ifc' || fmt === 'revit') return defaultThumbnailPath('equipment')
  if (fmt.includes('gltf') || fmt === 'glb') return defaultThumbnailPath('structure')
  return defaultThumbnailPath('default')
}

export function isTooSmallBimModel(fileSizeMb?: number | null, sizeBytes?: number | null): boolean {
  if (sizeBytes != null && sizeBytes > 0) {
    return sizeBytes < BIM_MIN_MODEL_BYTES
  }
  if (fileSizeMb == null) return false
  return fileSizeMb * 1024 * 1024 < BIM_MIN_MODEL_BYTES
}
