/** BIM アップロード対象のファイル種別を拡張子・MIME から判別 */

export type BimFileKind = 'model' | 'image' | 'unknown'

export function detectBimFileKind(file: File): BimFileKind {
  const name = file.name.toLowerCase()
  const type = file.type.toLowerCase()

  if (
    name.endsWith('.glb') ||
    name.endsWith('.gltf') ||
    type === 'model/gltf-binary' ||
    type === 'model/gltf+json' ||
    type.includes('gltf')
  ) {
    return 'model'
  }

  if (
    type.startsWith('image/') ||
    name.endsWith('.png') ||
    name.endsWith('.jpg') ||
    name.endsWith('.jpeg') ||
    name.endsWith('.webp') ||
    name.endsWith('.gif') ||
    name.endsWith('.svg')
  ) {
    return 'image'
  }

  return 'unknown'
}
