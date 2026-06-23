/** BIM サムネイル種別（DB / タイトル / フォーマットから解決）。 */
export type BimThumbKind = 'default' | 'structure' | 'equipment' | 'renovation'

export const BIM_SAMPLE_MODEL_HELMET =
  'https://cdn.jsdelivr.net/gh/KhronosGroup/glTF-Sample-Assets@main/Models/DamagedHelmet/glTF-Binary/DamagedHelmet.glb'

export const BIM_SAMPLE_MODEL_ASTRONAUT =
  'https://cdn.jsdelivr.net/gh/KhronosGroup/glTF-Sample-Assets@main/Models/Astronaut/glTF-Binary/Astronaut.glb'

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
  const url = (viewerUrl ?? '').trim()
  if (url.includes('/api/saas/bim/files/')) {
    return url
  }
  if (url && !url.includes('modelviewer.dev') && (url.endsWith('.glb') || url.endsWith('.gltf'))) {
    return url
  }
  const fmt = (format ?? '').toLowerCase()
  if (fmt.includes('gltf') || fmt === 'glb') {
    if (url.includes('Astronaut')) return BIM_SAMPLE_MODEL_ASTRONAUT
    return BIM_SAMPLE_MODEL_HELMET
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
