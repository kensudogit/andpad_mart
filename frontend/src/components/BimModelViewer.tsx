'use client'

import { createElement, useEffect, useRef, useState } from 'react'
import { resolveBimModelObjectUrl } from '@/lib/bim-model-fetch'
import { ui } from '@/lib/ui'

export const MODEL_VIEWER_SCRIPT =
  'https://ajax.googleapis.com/ajax/libs/model-viewer/3.5.0/model-viewer.min.js'

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

/** Google model-viewer で glTF / GLB を表示（認証付き API パス対応） */
export function BimModelViewer({ src, alt }: BimModelViewerProps) {
  const viewerRef = useRef<HTMLElement | null>(null)
  const revokeRef = useRef<(() => void) | null>(null)
  const [displaySrc, setDisplaySrc] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    setError(null)
    setDisplaySrc(null)
    revokeRef.current?.()
    revokeRef.current = null

    void (async () => {
      try {
        const resolved = await resolveBimModelObjectUrl(src)
        if (cancelled) {
          resolved.revoke()
          return
        }
        revokeRef.current = resolved.revoke
        setDisplaySrc(resolved.url)
      } catch (err) {
        if (!cancelled) {
          setError(err instanceof Error ? err.message : ui.bimViewerLoadError)
        }
      } finally {
        if (!cancelled) setLoading(false)
      }
    })()

    return () => {
      cancelled = true
      revokeRef.current?.()
      revokeRef.current = null
    }
  }, [src])

  useEffect(() => {
    const el = viewerRef.current
    if (!el || !displaySrc) return

    el.setAttribute('src', displaySrc)
    el.setAttribute('alt', alt)
    el.setAttribute('camera-controls', '')
    el.setAttribute('auto-rotate', '')
    el.setAttribute('shadow-intensity', '1')
    el.setAttribute('exposure', '1')
    el.setAttribute('interaction-prompt', 'none')
    el.style.width = '100%'
    el.style.height = '100%'
    el.style.display = 'block'
    el.style.background = '#1a1f2e'

    const onError = () => setError(ui.bimViewerLoadError)
    const onLoad = () => setError(null)
    el.addEventListener('error', onError)
    el.addEventListener('load', onLoad)
    return () => {
      el.removeEventListener('error', onError)
      el.removeEventListener('load', onLoad)
    }
  }, [displaySrc, alt])

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

  return createElement('model-viewer', {
    ref: viewerRef,
    className: 'bim-model-viewer',
  })
}
