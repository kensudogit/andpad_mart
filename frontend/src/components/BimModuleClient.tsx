'use client'

/**
 * BIM クラウドビューワ（モデル登録・model-viewer 表示）。
 */
import Script from 'next/script'
import Link from 'next/link'
import { useMutation, useQuery } from '@apollo/client/react'
import { createElement, useEffect, useState } from 'react'
import {
  BimModelsDocument,
  ConstructionProjectsDocument,
  CreateBimModelDocument,
} from '@/lib/generated/graphql'
import {
  BIM_SAMPLE_MODEL_HELMET,
  BIM_THUMB_DEFAULT,
  BIM_THUMB_STRUCTURE,
  canUseModelViewer,
  isEmbeddableViewerPage,
  resolveBimThumbnail,
} from '@/lib/bim-assets'
import { graphQLErrorHint, isAuthRequiredGraphQLError } from '@/lib/graphql-errors'
import { ui } from '@/lib/ui'

function BimThumbnail({ src, className }: { src: string; className?: string }) {
  const [failed, setFailed] = useState(false)
  const resolved = failed ? BIM_THUMB_DEFAULT : resolveBimThumbnail(src)

  return (
    <img
      src={resolved}
      alt=""
      className={className}
      loading="lazy"
      onError={() => setFailed(true)}
    />
  )
}

/** BIM モデル一覧・登録・3D ビューワ */
export function BimModuleClient() {
  const [title, setTitle] = useState('')
  const [format, setFormat] = useState('glTF')
  const [viewerUrl, setViewerUrl] = useState(BIM_SAMPLE_MODEL_HELMET)
  const [thumbnailUrl, setThumbnailUrl] = useState(BIM_THUMB_STRUCTURE)
  const [fileSize, setFileSize] = useState('')
  const [projectId, setProjectId] = useState('')
  const [selectedId, setSelectedId] = useState<string | null>(null)
  const [modelViewerReady, setModelViewerReady] = useState(false)

  const { data: projectsData } = useQuery(ConstructionProjectsDocument, { fetchPolicy: 'network-only' })
  const { data, loading, error, refetch } = useQuery(BimModelsDocument, {
    variables: { projectId: projectId || undefined },
    fetchPolicy: 'network-only',
  })
  const [create, { loading: busy }] = useMutation(CreateBimModelDocument, {
    onCompleted: (res) => {
      setTitle('')
      setSelectedId(res.createBimModel.id)
      refetch()
    },
  })

  const projects = projectsData?.constructionProjects ?? []
  const models = data?.bimModels ?? []
  const selected = models.find((m) => m.id === selectedId) ?? models[0] ?? null
  const showModelViewer = selected ? canUseModelViewer(selected.format, selected.viewerUrl) : false
  const showIframe = selected ? isEmbeddableViewerPage(selected.viewerUrl) : false
  const posterUrl = selected ? resolveBimThumbnail(selected.thumbnailUrl) : BIM_THUMB_DEFAULT

  useEffect(() => {
    if (!projectId && projects.length > 0) setProjectId(projects[0].id)
    if (!selectedId && models.length > 0) setSelectedId(models[0].id)
  }, [projectId, projects, selectedId, models])

  if (loading) return <p className="muted">{ui.boardLoading}</p>

  if (error) {
    const msg = isAuthRequiredGraphQLError(error)
      ? ui.saasLoginHint
      : error.message || graphQLErrorHint(error.message)
    return <p className="alert">{msg}</p>
  }

  return (
    <>
      <Script
        type="module"
        src="https://ajax.googleapis.com/ajax/libs/model-viewer/3.5.0/model-viewer.min.js"
        strategy="afterInteractive"
        onReady={() => setModelViewerReady(true)}
      />
      <div className="page-head">
        <Link href="/saas" className="muted">
          {ui.saasBack}
        </Link>
        <h1>{ui.bimTitle}</h1>
        <p className="muted">{ui.bimDesc}</p>
      </div>

      <section className="saas-panel">
        <h2>{ui.bimNew}</h2>
        <div className="saas-form">
          <select value={projectId} onChange={(e) => setProjectId(e.target.value)}>
            {projects.map((p) => (
              <option key={p.id} value={p.id}>
                {p.name}
              </option>
            ))}
          </select>
          <input value={title} onChange={(e) => setTitle(e.target.value)} placeholder={ui.saasTitle} />
          <select value={format} onChange={(e) => setFormat(e.target.value)}>
            <option value="IFC">IFC</option>
            <option value="glTF">glTF</option>
            <option value="Revit">Revit</option>
          </select>
          <input value={viewerUrl} onChange={(e) => setViewerUrl(e.target.value)} placeholder={ui.bimViewerUrl} />
          <input value={thumbnailUrl} onChange={(e) => setThumbnailUrl(e.target.value)} placeholder={ui.bimThumbnailUrl} />
          <input type="number" value={fileSize} onChange={(e) => setFileSize(e.target.value)} placeholder={ui.bimFileSize} />
          <button
            type="button"
            className="btn"
            disabled={busy || !title.trim() || !projectId}
            onClick={() =>
              create({
                variables: {
                  input: {
                    projectId,
                    title,
                    format,
                    viewerUrl,
                    thumbnailUrl: thumbnailUrl || undefined,
                    fileSizeMb: fileSize ? parseFloat(fileSize) : undefined,
                  },
                },
              })
            }
          >
            {ui.saasCreate}
          </button>
        </div>
      </section>

      <div className="bim-layout">
        <section className="saas-panel">
          <h2>{ui.bimSelectModel}</h2>
          {models.length === 0 ? (
            <p className="muted">{ui.bimNoModel}</p>
          ) : (
            <ul className="bim-model-list">
              {models.map((m) => (
                <li key={m.id}>
                  <button
                    type="button"
                    className={`btn bim-model-item${selected?.id === m.id ? '' : ' btn-ghost'}`}
                    onClick={() => setSelectedId(m.id)}
                  >
                    <BimThumbnail src={m.thumbnailUrl} className="bim-model-thumb" />
                    <span className="bim-model-body">
                      <strong>{m.title}</strong>
                      <span className="bim-model-meta">
                        {m.format} · {m.projectName}
                        {m.fileSizeMb != null ? ` · ${m.fileSizeMb}MB` : ''}
                      </span>
                    </span>
                  </button>
                </li>
              ))}
            </ul>
          )}
        </section>

        <section className="saas-panel">
          <h2>{ui.bimViewer}</h2>
          {selected ? (
            <>
              <p className="muted small">
                {selected.title} ({selected.format}) — {selected.uploadedBy}
              </p>
              <div className="bim-viewer-frame">
                {showModelViewer && modelViewerReady ? (
                  createElement('model-viewer', {
                    key: selected.id,
                    src: selected.viewerUrl,
                    poster: posterUrl,
                    alt: selected.title,
                    'camera-controls': true,
                    'auto-rotate': true,
                    'shadow-intensity': '1',
                    style: { width: '100%', height: '100%' },
                  })
                ) : showModelViewer ? (
                  <div className="bim-viewer-poster">
                    <BimThumbnail src={selected.thumbnailUrl} className="bim-viewer-poster-img" />
                    <p className="muted small">{ui.boardLoading}</p>
                  </div>
                ) : showIframe ? (
                  <iframe
                    title={selected.title}
                    src={selected.viewerUrl}
                    style={{ width: '100%', height: '100%', border: 'none' }}
                    allow="fullscreen"
                  />
                ) : (
                  <div className="bim-viewer-poster">
                    <BimThumbnail src={selected.thumbnailUrl} className="bim-viewer-poster-img" />
                    <p className="muted small bim-viewer-poster-hint">{ui.bimViewerPosterHint}</p>
                  </div>
                )}
              </div>
            </>
          ) : (
            <p className="muted">{ui.bimNoModel}</p>
          )}
        </section>
      </div>
    </>
  )
}
