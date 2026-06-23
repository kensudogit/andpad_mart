'use client'

import { createElement, useEffect, useLayoutEffect, useRef, useState } from 'react'
import { resolveBimModelObjectUrl } from '@/lib/bim-model-fetch'
import { ui } from '@/lib/ui'

export const MODEL_VIEWER_SCRIPT =
  'https://ajax.googleapis.com/ajax/libs/model-viewer/3.5.0/model-viewer.min.js'

const DRACO_DECODER_PATH = 'https://www.gstatic.com/draco/versioned/decoders/1.5.7/'
const KTX2_TRANSCODER_PATH =
  'https://ajax.googleapis.com/ajax/libs/model-viewer/3.5.0/lib/basis/'

type ModelViewerElement = HTMLElement & {
  loaded?: Promise<void>
}

export function useModelViewerReady() {
  const [ready, setReady] = useState(false)

  useEffect(() => {
    if (typeof window === 'undefined') return

    const markReady = () => {
      if (customElements.get('model-viewer')) {
        setReady(true)
        return true
      }
      return false
    }

    if (markReady()) return

    customElements.whenDefined('model-viewer').then(() => setReady(true))

    const existing = document.querySelector(`script[src="${MODEL_VIEWER_SCRIPT}"]`)
    if (!existing) {
      const script = document.createElement('script')
      script.type = 'module'
      script.src = MODEL_VIEWER_SCRIPT
      script.async = true
      document.head.appendChild(script)
    }
  }, [])

  return ready
}

type BimModelViewerProps = {
  src: string
  alt: string
}

function modelViewerErrorMessage(event: Event): string {
  const detail = (event as CustomEvent<{ message?: string }>).detail
  const message = detail?.message?.trim()
  if (!message) return ui.bimViewerLoadError
  if (message.includes('DRACO') || message.includes('draco')) {
    return 'Draco 圧縮モデルの読み込みに失敗しました。GLB を再エクスポートしてお試しください'
  }
  return message
}

/** Google model-viewer で glTF / GLB を表示（認証付き API パス対応） */
export function BimModelViewer({ src, alt }: BimModelViewerProps) {
  const viewerRef = useRef<ModelViewerElement | null>(null)
  const revokeRef = useRef<(() => void) | null>(null)
  const activeRef = useRef(true)
  const [displaySrc, setDisplaySrc] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    activeRef.current = true
    setLoading(true)
    setError(null)
    setDisplaySrc(null)
    revokeRef.current?.()
    revokeRef.current = null

    void (async () => {
      try {
        const resolved = await resolveBimModelObjectUrl(src)
        if (!activeRef.current) {
          resolved.revoke()
          return
        }
        revokeRef.current = resolved.revoke
        setDisplaySrc(resolved.url)
      } catch (err) {
        if (activeRef.current) {
          setError(err instanceof Error ? err.message : ui.bimViewerLoadError)
        }
      } finally {
        if (activeRef.current) setLoading(false)
      }
    })()

    return () => {
      activeRef.current = false
      revokeRef.current?.()
      revokeRef.current = null
    }
  }, [src])

  useLayoutEffect(() => {
    const el = viewerRef.current
    if (!el || !displaySrc) return

    let cancelled = false

    const onError = (event: Event) => {
      if (cancelled || !activeRef.current) return
      setError(modelViewerErrorMessage(event))
    }

    el.addEventListener('error', onError)

    if (el.loaded) {
      void el.loaded
        .then(() => {
          if (!cancelled && activeRef.current) setError(null)
        })
        .catch((err: unknown) => {
          if (!cancelled && activeRef.current) {
            const message = err instanceof Error ? err.message : ui.bimViewerLoadError
            setError(message)
          }
        })
    }

    return () => {
      cancelled = true
      el.removeEventListener('error', onError)
    }
  }, [displaySrc])

  if (loading) {
    return (
      <div className="bim-viewer-poster">
        <p className="muted small">{ui.bimViewerLoading}</p>
      </div>
    )
  }

  if (error) {
    return (
      <div className="bim-viewer-poster">
        <p className="alert small">{error}</p>
      </div>
    )
  }

  if (!displaySrc) {
    return (
      <div className="bim-viewer-poster">
        <p className="alert small">{ui.bimViewerLoadError}</p>
      </div>
    )
  }

  return createElement('model-viewer', {
    key: displaySrc,
    ref: viewerRef,
    className: 'bim-model-viewer',
    src: displaySrc,
    alt,
    'camera-controls': '',
    'auto-rotate': '',
    'shadow-intensity': '1',
    'exposure': '1',
    'interaction-prompt': 'none',
    'draco-decoder-path': DRACO_DECODER_PATH,
    'ktx2-transcoder-path': KTX2_TRANSCODER_PATH,
    style: {
      width: '100%',
      height: '100%',
      display: 'block',
      background: '#1a1f2e',
    },
  })
}
