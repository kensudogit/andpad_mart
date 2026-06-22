'use client'

/**
 * 資料承認画面 — 未完了案件確認一覧（CnfmActvMatterList / PostgreSQL）。
 */
import { useQuery } from '@apollo/client/react'
import { useEffect } from 'react'
import {
  CnfmActvMattersDocument,
  LumpCnfmActvMattersDocument,
  type CnfmActvMattersQuery,
  type LumpCnfmActvMattersQuery,
} from '@/lib/generated/graphql'
import { ui } from '@/lib/ui'

type DocApprovalCnfmActvPanelProps = {
  refreshKey?: number
}

type ConfirmRow = CnfmActvMattersQuery['cnfmActvMatters'][number]
type LumpRow = LumpCnfmActvMattersQuery['lumpCnfmActvMatters'][number]

function confirmFlagLabel(flag: string) {
  return flag === '1' ? ui.docApprovalCnfmFlagDone : ui.docApprovalCnfmFlagPending
}

function confirmFlagTone(flag: string): 'green' | 'amber' {
  return flag === '1' ? 'green' : 'amber'
}

function MatterCard({ row }: { row: ConfirmRow | LumpRow }) {
  return (
    <article className="doc-approval-cnfm-item">
      <header className="doc-approval-cnfm-head">
        <div className="doc-approval-cnfm-head-main">
          <strong>{row.matterName || row.systemMatterId}</strong>
          <span className="muted small">{row.flowName || row.flowId}</span>
        </div>
        <span
          className={`doc-approval-cnfm-badge doc-approval-cnfm-badge--${confirmFlagTone(row.confirmCplFlag)}`}
        >
          {confirmFlagLabel(row.confirmCplFlag)}
        </span>
      </header>
      <dl className="doc-approval-cnfm-meta">
        <div>
          <dt>{ui.docApprovalCnfmSystemMatterId}</dt>
          <dd>{row.systemMatterId}</dd>
        </div>
        <div>
          <dt>{ui.docApprovalCnfmMatterNumber}</dt>
          <dd>{row.matterNumber || '—'}</dd>
        </div>
        <div>
          <dt>{ui.docApprovalCnfmNode}</dt>
          <dd>{row.nodeId || '—'}</dd>
        </div>
        <div>
          <dt>{ui.docApprovalCnfmApplicant}</dt>
          <dd>{row.applyAuthUserName || row.applyAuthUserCode || '—'}</dd>
        </div>
        <div>
          <dt>{ui.docApprovalCnfmApplyDate}</dt>
          <dd>{row.applyDate || '—'}</dd>
        </div>
        <div>
          <dt>{ui.docApprovalCnfmArrivedDate}</dt>
          <dd>{row.arrivedDate || row.updatedAt?.slice(0, 19) || '—'}</dd>
        </div>
        {row.priorityLevel ? (
          <div>
            <dt>{ui.docApprovalCnfmPriority}</dt>
            <dd>{row.priorityLevel}</dd>
          </div>
        ) : null}
      </dl>
    </article>
  )
}

/** 未完了案件確認一覧パネル */
export function DocApprovalCnfmActvPanel({ refreshKey = 0 }: DocApprovalCnfmActvPanelProps) {
  const confirmQuery = useQuery(CnfmActvMattersDocument, {
    variables: { flowIds: ['document-approval'], limit: 20 },
    fetchPolicy: 'network-only',
  })
  const lumpQuery = useQuery(LumpCnfmActvMattersDocument, {
    variables: { flowIds: ['document-approval'], noOrgzConditionFlag: false, limit: 20 },
    fetchPolicy: 'network-only',
  })

  useEffect(() => {
    if (refreshKey > 0) {
      void confirmQuery.refetch()
      void lumpQuery.refetch()
    }
  }, [refreshKey, confirmQuery, lumpQuery])

  const confirmRows = confirmQuery.data?.cnfmActvMatters ?? []
  const lumpRows = lumpQuery.data?.lumpCnfmActvMatters ?? []
  const loading = confirmQuery.loading || lumpQuery.loading
  const error = confirmQuery.error || lumpQuery.error

  return (
    <section className="saas-panel doc-approval-cnfm-panel doc-approval-sub-panel">
      <h2>{ui.docApprovalCnfmTitle}</h2>
      <p className="muted small doc-approval-sub-panel-desc">{ui.docApprovalCnfmDesc}</p>
      {loading ? (
        <p className="muted">{ui.boardLoading}</p>
      ) : error ? (
        <p className="alert">{ui.docApprovalCnfmLoadFailed}</p>
      ) : (
        <div className="doc-approval-cnfm-sections">
          <div className="doc-approval-cnfm-section">
            <h3>{ui.docApprovalCnfmConfirmTitle}</h3>
            <p className="muted small">{ui.docApprovalCnfmConfirmDesc}</p>
            {confirmRows.length === 0 ? (
              <p className="muted">{ui.docApprovalCnfmConfirmEmpty}</p>
            ) : (
              <div className="doc-approval-cnfm-list">
                {confirmRows.map((row: ConfirmRow) => (
                  <MatterCard key={row.id} row={row} />
                ))}
              </div>
            )}
          </div>
          <div className="doc-approval-cnfm-section">
            <h3>{ui.docApprovalCnfmLumpTitle}</h3>
            <p className="muted small">{ui.docApprovalCnfmLumpDesc}</p>
            {lumpRows.length === 0 ? (
              <p className="muted">{ui.docApprovalCnfmLumpEmpty}</p>
            ) : (
              <div className="doc-approval-cnfm-list">
                {lumpRows.map((row: LumpRow) => (
                  <MatterCard key={row.id} row={row} />
                ))}
              </div>
            )}
          </div>
        </div>
      )}
    </section>
  )
}
