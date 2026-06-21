'use client'

/**
 * 資料承認画面 — フロー別モニタリング結果（MonitoringManager / PostgreSQL）。
 */
import { useQuery } from '@apollo/client/react'
import { useEffect } from 'react'
import { MonitoringFlowDataDocument, type MonitoringFlowDataQuery } from '@/lib/generated/graphql'
import { ui } from '@/lib/ui'

type DocApprovalMonitoringPanelProps = {
  refreshKey?: number
}

type MonitoringRow = MonitoringFlowDataQuery['monitoringFlowData'][number]

type StatCard = {
  key: string
  label: string
  value: string
  tone: 'green' | 'rose' | 'amber' | 'blue' | 'indigo'
}

function buildStats(row: MonitoringRow): StatCard[] {
  return [
    { key: 'approveEnd', label: ui.docApprovalMonitoringApproveEnd, value: row.approveEndCount, tone: 'green' },
    { key: 'approve', label: ui.docApprovalMonitoringCompleted, value: row.approveCount, tone: 'indigo' },
    { key: 'deny', label: ui.docApprovalMonitoringDeny, value: row.denyCount, tone: 'rose' },
    { key: 'discontinue', label: ui.docApprovalMonitoringDiscontinue, value: row.discontinueCount, tone: 'amber' },
    { key: 'handle', label: ui.docApprovalMonitoringHandle, value: row.matterHandleCount, tone: 'blue' },
    { key: 'average', label: ui.docApprovalMonitoringAverageTime, value: `${row.averageTime} ${ui.docApprovalMonitoringMinutes}`, tone: 'indigo' },
    { key: 'min', label: ui.docApprovalMonitoringMinTime, value: `${row.minimumTime} ${ui.docApprovalMonitoringMinutes}`, tone: 'blue' },
    { key: 'max', label: ui.docApprovalMonitoringMaxTime, value: `${row.maximumTime} ${ui.docApprovalMonitoringMinutes}`, tone: 'amber' },
  ]
}

/** フロー別モニタリング確認パネル */
export function DocApprovalMonitoringPanel({ refreshKey = 0 }: DocApprovalMonitoringPanelProps) {
  const { data, loading, error, refetch } = useQuery(MonitoringFlowDataDocument, {
    variables: { flowIds: ['document-approval'] },
    fetchPolicy: 'network-only',
  })

  useEffect(() => {
    if (refreshKey > 0) {
      void refetch()
    }
  }, [refreshKey, refetch])

  const rows = data?.monitoringFlowData ?? []

  return (
    <section className="saas-panel doc-approval-monitoring-panel">
      <h2>{ui.docApprovalMonitoringTitle}</h2>
      <p className="muted small">{ui.docApprovalMonitoringDesc}</p>
      {loading ? (
        <p className="muted">{ui.boardLoading}</p>
      ) : error ? (
        <p className="alert">{ui.docApprovalMonitoringLoadFailed}</p>
      ) : rows.length === 0 ? (
        <p className="muted">{ui.docApprovalMonitoringEmpty}</p>
      ) : (
        <div className="doc-approval-monitoring-list">
          {rows.map((row) => (
            <article key={row.flowId} className="doc-approval-monitoring-item">
              <header className="doc-approval-monitoring-head">
                <div>
                  <strong>{row.flowName || row.flowId}</strong>
                  <div className="muted small">{row.flowId}</div>
                </div>
                <div className="doc-approval-monitoring-total">
                  <span className="muted small">{ui.docApprovalMonitoringTotal}</span>
                  <strong>{row.countSum || row.approveCount}</strong>
                </div>
              </header>
              <div className="doc-approval-monitoring-stats">
                {buildStats(row).map((stat) => (
                  <div key={stat.key} className={`doc-approval-stat doc-approval-stat--${stat.tone}`}>
                    <span className="doc-approval-stat-label">{stat.label}</span>
                    <strong className="doc-approval-stat-value">{stat.value}</strong>
                  </div>
                ))}
              </div>
            </article>
          ))}
        </div>
      )}
    </section>
  )
}
