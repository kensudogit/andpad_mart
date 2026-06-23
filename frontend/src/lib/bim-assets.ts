/** BIM デモ用アセット（外部 CDN 障害時も表示できるよう自前 SVG を優先）。 */
export const BIM_SAMPLE_MODEL_HELMET =
  'https://cdn.jsdelivr.net/gh/KhronosGroup/glTF-Sample-Assets@main/Models/DamagedHelmet/glTF-Binary/DamagedHelmet.glb'

export const BIM_SAMPLE_MODEL_ASTRONAUT =
  'https://cdn.jsdelivr.net/gh/KhronosGroup/glTF-Sample-Assets@main/Models/Astronaut/glTF-Binary/Astronaut.glb'

export const BIM_THUMB_DEFAULT = '/bim/thumbs/default.svg'
export const BIM_THUMB_STRUCTURE = '/bim/thumbs/structure.svg'
export const BIM_THUMB_EQUIPMENT = '/bim/thumbs/equipment.svg'
export const BIM_THUMB_RENOVATION = '/bim/thumbs/renovation.svg'

export function resolveBimThumbnail(url?: string | null) {
  if (!url || url.trim() === '') return BIM_THUMB_DEFAULT
  if (url.startsWith('/')) return url
  if (url.includes('modelviewer.dev') || url.includes('unsplash.com')) return BIM_THUMB_DEFAULT
  return url
}

export function canUseModelViewer(format?: string | null, viewerUrl?: string | null) {
  const fmt = (format ?? '').toLowerCase()
  const url = (viewerUrl ?? '').toLowerCase()
  if (url.endsWith('.glb') || url.endsWith('.gltf')) return true
  return fmt.includes('gltf') || fmt === 'glb'
}

export function isEmbeddableViewerPage(viewerUrl?: string | null) {
  const url = (viewerUrl ?? '').trim().toLowerCase()
  if (!url.startsWith('http')) return false
  return !url.endsWith('.glb') && !url.endsWith('.gltf')
}
