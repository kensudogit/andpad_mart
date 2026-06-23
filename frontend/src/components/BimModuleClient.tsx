'use client'

/**
 * BIM クラウドビューワ（モデル登録・model-viewer 表示）。
 */
import Link from 'next/link'
import { useMutation, useQuery } from '@apollo/client/react'
import { createElement, useEffect, useState } from 'react'
import { BimModelThumbnail } from '@/components/BimModelThumbnail'
import {
  BimModelsDocument,
  ConstructionProjectsDocument,
  CreateBimModelDocument,
} from '@/lib/generated/graphql'
import {
  BIM_SAMPLE_MODEL_HELMET,
  canUseModelViewer,
  defaultThumbnailForFormat,
  getBimThumbKind,
  isEmbeddableViewerPage,
  resolveBimViewerUrl,
} from '@/lib/bim-assets'
import { uploadBimModel, uploadBimThumbnail } from '@/lib/bim-upload'
import { graphQLErrorHint, isAuthRequiredGraphQLError } from '@/lib/graphql-errors'
import { ui } from '@/lib/ui'

const MODEL_VIEWER_SRC =
  'https://ajax.googleapis.com/ajax/libs/model-viewer/3.5.0/model-viewer.min.js'

function useModelViewerReady() {
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

    const existing = document.querySelector(`script[src="${MODEL_VIEWER_SRC}"]`)
    if (!existing) {
      const script = document.createElement('script')
      script.type = 'module'
      script.src = MODEL_VIEWER_SRC
      script.async = true
      document.head.appendChild(script)
    }
  }, [])

  return ready
}

/** BIM モデル一覧・登録・3D ビューワ */
export function BimModuleClient() {
  const [title, setTitle] = useState('')
  const [format, setFormat] = useState('glTF')
  const [viewerUrl, setViewerUrl] = useState(BIM_SAMPLE_MODEL_HELMET)
  const [thumbnailUrl, setThumbnailUrl] = useState(defaultThumbnailForFormat('glTF'))
  const [fileSize, setFileSize] = useState('')
  const [projectId, setProjectId] = useState('')
  const [selectedId, setSelectedId] = useState<string | null>(null)
  const [uploadBusy, setUploadBusy] = useState(false)
  const [uploadMessage, setUploadMessage] = useState<string | null>(null)
  const modelViewerReady = useModelViewerReady()

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
  const selectedViewerUrl = selected ? resolveBimViewerUrl(selected.format, selected.viewerUrl) : ''
  const showModelViewer = selected ? canUseModelViewer(selected.format, selected.viewerUrl) : false
  const showIframe = selected ? isEmbeddableViewerPage(selected.viewerUrl) : false
  const selectedThumbKind = selected
    ? getBimThumbKind(selected.title, selected.format, selected.thumbnailUrl)
    : 'default'

  useEffect(() => {
    if (!projectId && projects.length > 0) setProjectId(projects[0].id)
    if (!selectedId && models.length > 0) setSelectedId(models[0].id)
  }, [projectId, projects, selectedId, models])

  async function handleThumbnailUpload(file: File, targetModelId?: string) {
    setUploadBusy(true)
    setUploadMessage(null)
    try {
      const result = await uploadBimThumbnail(file, targetModelId)
      if (!targetModelId) {
        setThumbnailUrl(result.url)
      }
      await refetch()
      setUploadMessage(ui.bimUploadDone)
    } catch (err) {
      const msg = err instanceof Error ? err.message : ui.bimUploadFailed
      setUploadMessage(msg)
    } finally {
      setUploadBusy(false)
    }
  }

  async function handleModelUpload(file: File, targetModelId?: string) {
    setUploadBusy(true)
    setUploadMessage(null)
    try {
      const result = await uploadBimModel(file, targetModelId)
      if (!targetModelId) {
        setViewerUrl(result.url)
        setFormat('glTF')
        if (result.fileSizeMb != null) {
          setFileSize(result.fileSizeMb.toFixed(1))
        }
      }
      await refetch()
      setUploadMessage(ui.bimModelUploadDone)
    } catch (err) {
      const msg = err instanceof Error ? err.message : ui.bimUploadFailed
      setUploadMessage(msg)
    } finally {
      setUploadBusy(false)
    }
  }

  if (loading) return <p className="muted">{ui.boardLoading}</p>

  if (error) {
    const msg = isAuthRequiredGraphQLError(error)
      ? ui.saasLoginHint
      : error.message || graphQLErrorHint(error.message)
    return <p className="alert">{msg}</p>
  }

  return (
    <>
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
          <select
            value={format}
            onChange={(e) => {
              const next = e.target.value
              setFormat(next)
              setThumbnailUrl(defaultThumbnailForFormat(next))
            }}
          >
            <option value="IFC">IFC</option>
            <option value="glTF">glTF</option>
            <option value="Revit">Revit</option>
          </select>
          <input value={viewerUrl} onChange={(e) => setViewerUrl(e.target.value)} placeholder={ui.bimViewerUrl} />
          <label className="btn btn-ghost bim-upload-btn">
            {uploadBusy ? ui.bimUploading : ui.bimUploadModel}
            <input
              type="file"
              accept=".glb,.gltf,model/gltf-binary,model/gltf+json"
              className="bim-upload-input"
              disabled={uploadBusy}
              onChange={(e) => {
                const file = e.target.files?.[0]
                if (file) void handleModelUpload(file)
                e.target.value = ''
              }}
            />
          </label>
          <input value={thumbnailUrl} onChange={(e) => setThumbnailUrl(e.target.value)} placeholder={ui.bimThumbnailUrl} />
          <label className="btn btn-ghost bim-upload-btn">
            {uploadBusy ? ui.bimUploading : ui.bimUploadThumbnail}
            <input
              type="file"
              accept="image/png,image/jpeg,image/webp,image/gif,image/svg+xml"
              className="bim-upload-input"
              disabled={uploadBusy}
              onChange={(e) => {
                const file = e.target.files?.[0]
                if (file) void handleThumbnailUpload(file)
                e.target.value = ''
              }}
            />
          </label>
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
        {uploadMessage ? (
          <p
            className={`small${
              uploadMessage === ui.bimUploadDone || uploadMessage === ui.bimModelUploadDone
                ? ' text-ok'
                : ' alert'
            }`}
          >
            {uploadMessage}
          </p>
        ) : null}
        {thumbnailUrl ? (
          <div className="bim-thumb-preview">
            <BimModelThumbnail
              title={title || ui.bimUploadPreview}
              format={format}
              thumbnailUrl={thumbnailUrl}
              kind={getBimThumbKind(title, format, thumbnailUrl)}
              className="bim-thumb-preview-img"
            />
          </div>
        ) : null}
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
                    <BimModelThumbnail
                      title={m.title}
                      format={m.format}
                      thumbnailUrl={m.thumbnailUrl}
                      kind={getBimThumbKind(m.title, m.format, m.thumbnailUrl)}
                      className="bim-model-thumb"
                    />
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
              <div className="bim-viewer-toolbar">
                <label className="btn btn-ghost bim-upload-btn">
                  {uploadBusy ? ui.bimUploading : ui.bimUpdateModel}
                  <input
                    type="file"
                    accept=".glb,.gltf,model/gltf-binary,model/gltf+json"
                    className="bim-upload-input"
                    disabled={uploadBusy}
                    onChange={(e) => {
                      const file = e.target.files?.[0]
                      if (file) void handleModelUpload(file, selected.id)
                      e.target.value = ''
                    }}
                  />
                </label>
                <label className="btn btn-ghost bim-upload-btn">
                  {uploadBusy ? ui.bimUploading : ui.bimUpdateThumbnail}
                  <input
                    type="file"
                    accept="image/png,image/jpeg,image/webp,image/gif,image/svg+xml"
                    className="bim-upload-input"
                    disabled={uploadBusy}
                    onChange={(e) => {
                      const file = e.target.files?.[0]
                      if (file) void handleThumbnailUpload(file, selected.id)
                      e.target.value = ''
                    }}
                  />
                </label>
              </div>
              <div className="bim-viewer-frame">
                {showModelViewer && modelViewerReady ? (
                  createElement('model-viewer', {
                    key: `${selected.id}:${selectedViewerUrl}`,
                    src: selectedViewerUrl,
                    alt: selected.title,
                    'camera-controls': true,
                    'auto-rotate': true,
                    'shadow-intensity': '1',
                    'environment-image': 'neutral',
                    'exposure': '1',
                    style: { width: '100%', height: '100%', background: '#1a1f2e' },
                  })
                ) : showModelViewer ? (
                  <div className="bim-viewer-poster">
                    <BimModelThumbnail
                      title={selected.title}
                      format={selected.format}
                      thumbnailUrl={selected.thumbnailUrl}
                      kind={selectedThumbKind}
                      className="bim-viewer-poster-img"
                      large
                    />
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
                    <BimModelThumbnail
                      title={selected.title}
                      format={selected.format}
                      thumbnailUrl={selected.thumbnailUrl}
                      kind={selectedThumbKind}
                      className="bim-viewer-poster-img"
                      large
                    />
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
