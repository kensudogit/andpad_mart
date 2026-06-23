import type { BimThumbKind } from '@/lib/bim-assets'
import { isUploadedBimThumbnail, resolveBimThumbnailSrc } from '@/lib/bim-upload'
import { BimThumbnailArt } from '@/components/BimThumbnailArt'

type BimModelThumbnailProps = {
  title: string
  format: string
  thumbnailUrl?: string | null
  kind: BimThumbKind
  className?: string
  large?: boolean
}

/** アップロード画像またはフォールバック SVG サムネイル。 */
export function BimModelThumbnail({
  title,
  format,
  thumbnailUrl,
  kind,
  className,
  large,
}: BimModelThumbnailProps) {
  const uploadedSrc = isUploadedBimThumbnail(thumbnailUrl)
    ? resolveBimThumbnailSrc(thumbnailUrl)
    : null

  if (uploadedSrc) {
    return (
      <img
        src={uploadedSrc}
        alt={`${title} (${format})`}
        className={className}
        loading="lazy"
      />
    )
  }

  return <BimThumbnailArt kind={kind} className={className} large={large} />
}
