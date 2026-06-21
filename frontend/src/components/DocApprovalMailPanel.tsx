'use client'

/**
 * 資料承認画面 — 送信メール一覧（PostgreSQL 保存分）。
 */
import { useQuery } from '@apollo/client/react'
import { useEffect } from 'react'
import { SentMailMessagesDocument } from '@/lib/generated/graphql'
import { ui } from '@/lib/ui'

function fmtDateTime(s?: string | null) {
  if (!s) return '—'
  return s.replace('T', ' ').slice(0, 19)
}

type DocApprovalMailPanelProps = {
  refreshKey?: number
}

/** 送信メール確認パネル */
export function DocApprovalMailPanel({ refreshKey = 0 }: DocApprovalMailPanelProps) {
  const { data, loading, refetch } = useQuery(SentMailMessagesDocument, {
    variables: { entityType: 'DOCUMENT', limit: 50 },
    fetchPolicy: 'network-only',
  })

  useEffect(() => {
    if (refreshKey > 0) {
      void refetch()
    }
  }, [refreshKey, refetch])

  const messages = data?.sentMailMessages ?? []

  return (
    <section className="saas-panel doc-approval-mail-panel">
      <h2>{ui.docApprovalSentMailTitle}</h2>
      <p className="muted small">{ui.docApprovalSentMailDesc}</p>
      {loading ? (
        <p className="muted">{ui.boardLoading}</p>
      ) : messages.length === 0 ? (
        <p className="muted">{ui.docApprovalSentMailEmpty}</p>
      ) : (
        <div className="doc-approval-mail-list">
          {messages.map((m) => (
            <article key={m.id} className="doc-approval-mail-item">
              <header className="doc-approval-mail-head">
                <strong>{m.subject}</strong>
                <span className="muted small">{fmtDateTime(m.createdAt)}</span>
              </header>
              <dl className="doc-approval-mail-meta">
                <div>
                  <dt>{ui.docApprovalMailTo}</dt>
                  <dd>{m.recipients || '—'}</dd>
                </div>
                <div>
                  <dt>{ui.docApprovalMailTemplate}</dt>
                  <dd>{m.mailId || '—'}</dd>
                </div>
                <div>
                  <dt>{ui.docApprovalMailEntity}</dt>
                  <dd>{m.entityId || '—'}</dd>
                </div>
              </dl>
              <pre className="doc-approval-mail-body">{m.body}</pre>
            </article>
          ))}
        </div>
      )}
    </section>
  )
}
