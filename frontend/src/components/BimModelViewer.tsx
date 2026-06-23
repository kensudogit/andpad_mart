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

  useEffect(() => {
    const el = viewerRef.current
    if (!el || !displaySrc) return

    const onError = (event: Event) => {
      if (!activeRef.current) return
      const detail = (event as CustomEvent<{ message?: string }>).detail?.message
      setError(detail?.trim() || ui.bimViewerLoadError)
    }
    const onLoad = () => {
      if (activeRef.current) setError(null)
    }

    el.addEventListener('error', onError)
    el.addEventListener('load', onLoad)
    return () => {
      el.removeEventListener('error', onError)
      el.removeEventListener('load', onLoad)
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
    style: {
      width: '100%',
      height: '100%',
      display: 'block',
      background: '#1a1f2e',
    },
  })
}
