'use client'

/**
 * 建設モジュール共通 UI（案件別モジュール記録の一覧・新規登録）。
 */
import Link from 'next/link'
import { useMutation, useQuery } from '@apollo/client/react'
import { useEffect, useMemo, useState } from 'react'
import { DocumentApprovalWorkflowDiagram } from '@/components/DocumentApprovalWorkflowDiagram'
import { DocApprovalAsyncProcessPanel } from '@/components/DocApprovalAsyncProcessPanel'
import { DocApprovalCnfmActvPanel } from '@/components/DocApprovalCnfmActvPanel'
import { DocApprovalMailPanel } from '@/components/DocApprovalMailPanel'
import { DocApprovalMonitoringPanel } from '@/components/DocApprovalMonitoringPanel'
import { DocApprovalStampPanel } from '@/components/DocApprovalStampPanel'
import {
  ConstructionProjectsDocument,
  CreateProjectModuleRecordDocument,
  ProjectModuleRecordsDocument,
} from '@/lib/generated/graphql'
import { graphQLErrorHint, isAuthRequiredGraphQLError } from '@/lib/graphql-errors'
import {
  type ConstructionModuleSlug,
  slugToLabel,
  slugToModuleCode,
} from '@/lib/construction-modules'
import { ui } from '@/lib/ui'

function fmtDate(s?: string | null) {
  if (!s) return '—'
  return s.slice(0, 10)
}

const RECORDS_PAGE_SIZE = 10

/** GraphQL エラーをユーザー向け文言に変換 */
function gqlError(err: { message?: string } | undefined, fallback: string) {
  if (!err) return null
  const msg = err.message ?? ''
  const lower = msg.toLowerCase()
  if (lower.includes('module not enabled') || lower.includes('moduledisabled')) {
    return ui.moduleDisabledHint
  }
  if (isAuthRequiredGraphQLError(err as Parameters<typeof isAuthRequiredGraphQLError>[0])) {
    return ui.saasLoginHint
  }
  return msg || graphQLErrorHint(err.message) || fallback
}

/** 指定 slug の建設モジュール画面 */
export function ConstructionModuleClient({ module: slug }: { module: ConstructionModuleSlug }) {
  const moduleCode = slugToModuleCode(slug)
  const label = slugToLabel(slug)

  const [title, setTitle] = useState('')
  const [detail, setDetail] = useState('')
  const [person, setPerson] = useState('')
  const [status, setStatus] = useState('OPEN')
  const [amount, setAmount] = useState('')
  const [projectId, setProjectId] = useState('__all__')
  const [mailRefreshKey, setMailRefreshKey] = useState(0)
  const [page, setPage] = useState(1)

  const { data: projectsData, loading: projectsLoading } = useQuery(ConstructionProjectsDocument, {
    fetchPolicy: 'network-only',
  })
  const { data, loading, error, refetch } = useQuery(ProjectModuleRecordsDocument, {
    variables: {
      moduleCode,
      projectId: projectId && projectId !== '__all__' ? projectId : undefined,
    },
    fetchPolicy: 'network-only',
  })
  const [createRecord, { loading: busy, error: mutErr }] = useMutation(CreateProjectModuleRecordDocument, {
    onCompleted: () => {
      setTitle('')
      setDetail('')
      setPerson('')
      setAmount('')
      refetch()
      if (slug === 'doc-approval') {
        setMailRefreshKey((k) => k + 1)
      }
    },
  })

  const projects = projectsData?.constructionProjects ?? []
  const items = data?.projectModuleRecords ?? []
  const totalPages = Math.max(1, Math.ceil(items.length / RECORDS_PAGE_SIZE))
  const pageItems = useMemo(() => {
    const start = (page - 1) * RECORDS_PAGE_SIZE
    return items.slice(start, start + RECORDS_PAGE_SIZE)
  }, [items, page])
  const err = gqlError(error ?? mutErr, ui.saasLoadFailed)

  useEffect(() => {
    setPage(1)
  }, [projectId, moduleCode])

  useEffect(() => {
    if (page > totalPages) {
      setPage(totalPages)
    }
  }, [page, totalPages])

  useEffect(() => {
    if (!projectId && projects.length > 0) {
      setProjectId('__all__')
    }
  }, [projectId, projects])

  return (
    <>
      <div className="page-head">
        <Link href="/saas" className="muted">
          {ui.saasBack}
        </Link>
        <h1>{label}</h1>
        <p className="muted">{ui.moduleDesc}</p>
      </div>

      {err && <p className="alert">{err}</p>}

      {slug === 'doc-approval' ? <DocumentApprovalWorkflowDiagram /> : null}

      <section className="saas-panel">
        <h2>{ui.moduleNewRecord}</h2>
        <div className="saas-form">
          <select
            value={projectId}
            onChange={(e) => setProjectId(e.target.value)}
            disabled={projectsLoading || projects.length === 0}
          >
            {projects.length === 0 ? (
              <option value="">{ui.noProjects}</option>
            ) : (
              <>
                <option value="__all__">{ui.allProjects}</option>
                {projects.map((p) => (
                  <option key={p.id} value={p.id}>
                    {p.name}
                  </option>
                ))}
              </>
            )}
          </select>
          <input value={title} onChange={(e) => setTitle(e.target.value)} placeholder={ui.saasTitle} />
          <input value={person} onChange={(e) => setPerson(e.target.value)} placeholder={ui.saasOwner} />
          <select value={status} onChange={(e) => setStatus(e.target.value)}>
            <option value="OPEN">{ui.statusOpen}</option>
            <option value="IN_PROGRESS">{ui.statusInProgress}</option>
            <option value="APPROVED">{ui.statusApproved}</option>
            <option value="DONE">{ui.statusDone}</option>
            {slug === 'e-delivery' && (
              <>
                <option value="SUBMITTED">{ui.edeliveryStatusSubmitted}</option>
                <option value="ACCEPTED">{ui.edeliveryStatusAccepted}</option>
              </>
            )}
            {slug === 'bm' && <option value="SCHEDULED">点検予定</option>}
          </select>
          {(slug === 'billing' || slug === 'inquiry-profit' || slug === 'orders' || slug === 'e-delivery') && (
            <input
              type="number"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              placeholder={ui.amountYen}
            />
          )}
          <button
            type="button"
            className="btn"
            disabled={busy || !title.trim() || !projectId || projectId === '__all__'}
            onClick={() =>
              createRecord({
                variables: {
                  input: {
                    projectId,
                    moduleCode,
                    title,
                    status,
                    detail,
                    personName: person,
                    amount: amount ? parseFloat(amount) : undefined,
                  },
                },
              })
            }
          >
            {ui.saasCreate}
          </button>
        </div>
        <textarea
          value={detail}
          onChange={(e) => setDetail(e.target.value)}
          placeholder={ui.saasDesc}
          rows={2}
          className="saas-textarea"
        />
        {projects.length === 0 && (
          <p className="muted">
            {ui.noProjectsHint}{' '}
            <Link href="/projects">{ui.navProjects}</Link>
          </p>
        )}
      </section>

      <section className="saas-panel">
        <h2>{ui.saasData}</h2>
        {loading ? (
          <p className="muted">{ui.boardLoading}</p>
        ) : (
          <div className="saas-table-wrap">
            <table className="saas-table">
              <thead>
                <tr>
                  <th>{ui.projectName}</th>
                  <th>{ui.saasTitle}</th>
                  <th>{ui.saasStatus}</th>
                  <th>{ui.saasOwner}</th>
                  <th>{ui.amountYen}</th>
                  <th>{ui.recordDate}</th>
                </tr>
              </thead>
              <tbody>
                {items.length === 0 ? (
                  <tr>
                    <td colSpan={6} className="saas-empty">
                      {ui.saasEmpty}
                    </td>
                  </tr>
                ) : (
                  pageItems.map((r) => (
                    <tr key={r.id}>
                      <td>{r.projectName}</td>
                      <td>
                        <strong>{r.title}</strong>
                        {r.detail ? <div className="muted small">{r.detail}</div> : null}
                      </td>
                      <td>
                        <span className="saas-status">{r.status}</span>
                      </td>
                      <td>{r.personName || '—'}</td>
                      <td>{r.amount != null ? `¥${r.amount.toLocaleString()}` : '—'}</td>
                      <td>{fmtDate(r.recordDate ?? r.createdAt)}</td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
            {items.length > RECORDS_PAGE_SIZE ? (
              <div className="pagination-bar saas-table-pagination">
                <button
                  type="button"
                  className="filter-link"
                  disabled={page <= 1}
                  onClick={() => setPage((p) => Math.max(1, p - 1))}
                >
                  {ui.prev}
                </button>
                <span className="saas-table-pagination-info">
                  {ui.pageOf(page, totalPages, items.length)}
                </span>
                <button
                  type="button"
                  className="filter-link"
                  disabled={page >= totalPages}
                  onClick={() => setPage((p) => Math.min(totalPages, p + 1))}
                >
                  {ui.next}
                </button>
              </div>
            ) : null}
          </div>
        )}
      </section>

      {slug === 'doc-approval' ? (
        <>
          <DocApprovalMonitoringPanel refreshKey={mailRefreshKey} />
          <DocApprovalCnfmActvPanel refreshKey={mailRefreshKey} />
          <DocApprovalAsyncProcessPanel refreshKey={mailRefreshKey} />
          <DocApprovalStampPanel refreshKey={mailRefreshKey} />
          <DocApprovalMailPanel refreshKey={mailRefreshKey} />
        </>
      ) : null}
    </>
  )
}
