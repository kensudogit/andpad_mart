'use client'

/**
 * 資料承認画面 — 完了案件印影一覧（CplMatterStampList / PostgreSQL）。
 */
import { useQuery } from '@apollo/client/react'
import { useEffect } from 'react'
import { MatterStampsDocument, type MatterStampsQuery } from '@/lib/generated/graphql'
import { ui } from '@/lib/ui'

type DocApprovalStampPanelProps = {
  refreshKey?: number
}

type StampRow = MatterStampsQuery['matterStamps'][number]

/** 印影確認パネル */
export function DocApprovalStampPanel({ refreshKey = 0 }: DocApprovalStampPanelProps) {
  const { data, loading, error, refetch } = useQuery(MatterStampsDocument, {
    variables: { flowId: 'document-approval', entityType: 'DOCUMENT', limit: 50 },
    fetchPolicy: 'network-only',
  })

  useEffect(() => {
    if (refreshKey > 0) {
      void refetch()
    }
  }, [refreshKey, refetch])

  const stamps = data?.matterStamps ?? []

  return (
    <section className="saas-panel doc-approval-stamp-panel">
      <h2>{ui.docApprovalStampTitle}</h2>
      <p className="muted small">{ui.docApprovalStampDesc}</p>
      {loading ? (
        <p className="muted">{ui.boardLoading}</p>
      ) : error ? (
        <p className="alert">{ui.docApprovalStampLoadFailed}</p>
      ) : stamps.length === 0 ? (
        <p className="muted">{ui.docApprovalStampEmpty}</p>
      ) : (
        <div className="doc-approval-stamp-list">
          {stamps.map((stamp, index) => (
            <article key={stamp.id ?? `${stamp.systemMatterId}-${stamp.no ?? index}`} className="doc-approval-stamp-item">
              <header className="doc-approval-stamp-head">
                <div>
                  <strong>{stamp.stampStr2 || stamp.nodeId || ui.docApprovalStampNodeFallback}</strong>
                  <div className="muted small">{stamp.stampType || '—'}</div>
                </div>
                <span className="muted small">{stamp.processDate || stamp.createdAt || '—'}</span>
              </header>
              <dl className="doc-approval-stamp-meta">
                <div>
                  <dt>{ui.docApprovalStampActor}</dt>
                  <dd>{stamp.stampStr1 || '—'}</dd>
                </div>
                <div>
                  <dt>{ui.docApprovalStampNode}</dt>
                  <dd>{stamp.nodeId || '—'}</dd>
                </div>
                <div>
                  <dt>{ui.docApprovalStampEntity}</dt>
                  <dd>{stamp.entityId || '—'}</dd>
                </div>
                <div>
                  <dt>{ui.docApprovalStampMatter}</dt>
                  <dd>{stamp.systemMatterId || '—'}</dd>
                </div>
              </dl>
            </article>
          ))}
        </div>
      )}
    </section>
  )
}
