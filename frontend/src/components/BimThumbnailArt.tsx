import type { BimThumbKind } from '@/lib/bim-assets'

type BimThumbnailArtProps = {
  kind: BimThumbKind
  className?: string
  large?: boolean
}

/** 外部画像に依存しない BIM サムネイル（インライン SVG）。 */
export function BimThumbnailArt({ kind, className, large }: BimThumbnailArtProps) {
  const label =
    kind === 'structure'
      ? '構造 BIM'
      : kind === 'equipment'
        ? '設備 BIM'
        : kind === 'renovation'
          ? '改修 BIM'
          : 'BIM'

  return (
    <svg
      className={className}
      xmlns="http://www.w3.org/2000/svg"
      viewBox="0 0 320 180"
      role="img"
      aria-label={label}
      preserveAspectRatio="xMidYMid meet"
    >
      {kind === 'structure' ? (
        <>
          <defs>
            <linearGradient id="bim-structure-bg" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" stopColor="#312e81" />
              <stop offset="100%" stopColor="#1e1b4b" />
            </linearGradient>
          </defs>
          <rect width="320" height="180" fill="url(#bim-structure-bg)" />
          <rect x="90" y="55" width="140" height="95" fill="none" stroke="#a5b4fc" strokeWidth="2.5" />
          <line x1="90" y1="85" x2="230" y2="85" stroke="#6366f1" strokeWidth="1.5" />
          <line x1="90" y1="115" x2="230" y2="115" stroke="#6366f1" strokeWidth="1.5" />
          <line x1="160" y1="55" x2="160" y2="150" stroke="#6366f1" strokeWidth="1.5" />
        </>
      ) : kind === 'equipment' ? (
        <>
          <defs>
            <linearGradient id="bim-equipment-bg" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" stopColor="#134e4a" />
              <stop offset="100%" stopColor="#042f2e" />
            </linearGradient>
          </defs>
          <rect width="320" height="180" fill="url(#bim-equipment-bg)" />
          <circle cx="160" cy="95" r="42" fill="none" stroke="#5eead4" strokeWidth="2.5" />
          <circle cx="160" cy="95" r="18" fill="none" stroke="#2dd4bf" strokeWidth="2" />
          <rect x="118" y="128" width="84" height="18" rx="4" fill="#14b8a6" opacity="0.35" />
        </>
      ) : kind === 'renovation' ? (
        <>
          <defs>
            <linearGradient id="bim-renovation-bg" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" stopColor="#78350f" />
              <stop offset="100%" stopColor="#451a03" />
            </linearGradient>
          </defs>
          <rect width="320" height="180" fill="url(#bim-renovation-bg)" />
          <path d="M70 140 L160 45 L250 140 Z" fill="none" stroke="#fdba74" strokeWidth="2.5" />
          <rect x="130" y="100" width="60" height="40" fill="#f97316" opacity="0.25" stroke="#fb923c" strokeWidth="1.5" />
        </>
      ) : (
        <>
          <defs>
            <linearGradient id="bim-default-bg" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" stopColor="#1e293b" />
              <stop offset="100%" stopColor="#0f172a" />
            </linearGradient>
          </defs>
          <rect width="320" height="180" fill="url(#bim-default-bg)" />
          <path d="M160 40 L240 150 H80 Z" fill="none" stroke="#818cf8" strokeWidth="3" />
          <path d="M80 150 H240" stroke="#6366f1" strokeWidth="2" />
        </>
      )}
      <text
        x="160"
        y={large ? 24 : 28}
        textAnchor="middle"
        fill={kind === 'equipment' ? '#99f6e4' : kind === 'renovation' ? '#fed7aa' : '#c7d2fe'}
        fontFamily="system-ui, sans-serif"
        fontSize={large ? 16 : 13}
        fontWeight="600"
      >
        {label}
      </text>
    </svg>
  )
}
