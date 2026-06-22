'use client'

/**
 * 資料承認画面 — 非同期処理状況（AsyncProcessWorkflow / PostgreSQL）。
 */
import { useQuery } from '@apollo/client/react'
import { useEffect } from 'react'
import { AsyncProcessStatusDataDocument, type AsyncProcessStatusDataQuery } from '@/lib/generated/graphql'
import { ui } from '@/lib/ui'

type DocApprovalAsyncProcessPanelProps = {
  refreshKey?: number
}

type AsyncRow = AsyncProcessStatusDataQuery['asyncProcessStatusData'][number]

function statusLabel(status: string) {
  switch (status) {
    case '0':
      return ui.docApprovalAsyncStatusAcceptedBefore
    case '1':
      return ui.docApprovalAsyncStatusAcceptedFail
    case '2':
      return ui.docApprovalAsyncStatusRunning
    case '3':
      return ui.docApprovalAsyncStatusSuccess
    case '4':
      return ui.docApprovalAsyncStatusError
    default:
      return status
  }
}

function statusTone(status: string): 'green' | 'rose' | 'amber' | 'blue' | 'indigo' {
  switch (status) {
    case '3':
      return 'green'
    case '4':
    case '1':
      return 'rose'
    case '2':
      return 'blue'
    default:
      return 'amber'
  }
}

/** 非同期処理状況確認パネル */
export function DocApprovalAsyncProcessPanel({ refreshKey = 0 }: DocApprovalAsyncProcessPanelProps) {
  const { data, loading, error, refetch } = useQuery(AsyncProcessStatusDataDocument, {
    variables: { flowIds: ['document-approval'], limit: 20 },
    fetchPolicy: 'network-only',
  })

  useEffect(() => {
    if (refreshKey > 0) {
      void refetch()
    }
  }, [refreshKey, refetch])

  const rows = data?.asyncProcessStatusData ?? []

  return (
    <section className="saas-panel doc-approval-async-panel doc-approval-sub-panel">
      <h2>{ui.docApprovalAsyncTitle}</h2>
      <p className="muted small doc-approval-sub-panel-desc">{ui.docApprovalAsyncDesc}</p>
      {loading ? (
        <p className="muted">{ui.boardLoading}</p>
      ) : error ? (
        <p className="alert">{ui.docApprovalAsyncLoadFailed}</p>
      ) : rows.length === 0 ? (
        <p className="muted">{ui.docApprovalAsyncEmpty}</p>
      ) : (
        <div className="doc-approval-async-list">
          {rows.map((row: AsyncRow) => (
            <article key={row.id} className="doc-approval-async-item">
              <header className="doc-approval-async-head">
                <div className="doc-approval-async-head-main">
                  <strong>{row.matterName || row.acceptId}</strong>
                  <span className="muted small">{row.flowId}</span>
                </div>
                <span className={`doc-approval-async-badge doc-approval-async-badge--${statusTone(row.asyncProcStatus)}`}>
                  {statusLabel(row.asyncProcStatus)}
                </span>
              </header>
              <dl className="doc-approval-async-meta">
                <div>
                  <dt>{ui.docApprovalAsyncAcceptId}</dt>
                  <dd>{row.acceptId}</dd>
                </div>
                <div>
                  <dt>{ui.docApprovalAsyncSystemMatterId}</dt>
                  <dd>{row.systemMatterId}</dd>
                </div>
                <div>
                  <dt>{ui.docApprovalAsyncNode}</dt>
                  <dd>{row.nodeId || '—'}</dd>
                </div>
                <div>
                  <dt>{ui.docApprovalAsyncProcDate}</dt>
                  <dd>{row.procDate || row.updatedAt?.slice(0, 19) || '—'}</dd>
                </div>
                {row.message ? (
                  <div className="doc-approval-async-message">
                    <dt>{ui.docApprovalAsyncMessage}</dt>
                    <dd>{row.message}</dd>
                  </div>
                ) : null}
                {row.subMessage ? (
                  <div>
                    <dt>{ui.docApprovalAsyncSubMessage}</dt>
                    <dd>{row.subMessage}</dd>
                  </div>
                ) : null}
              </dl>
            </article>
          ))}
        </div>
      )}
    </section>
  )
}
