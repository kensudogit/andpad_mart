'use client'

/**
 * 資料承認（document-approval）ワークフロー図。
 */
import { ui } from '@/lib/ui'

const steps = [
  {
    key: 'submit',
    num: '1',
    title: ui.docApprovalStepDraft,
    actor: ui.docApprovalActorSubmitter,
    action: ui.docApprovalActionSubmit,
    tone: 'indigo',
    imNode: null,
  },
  {
    key: 'review',
    num: '2',
    title: ui.docApprovalStepReview,
    actor: ui.docApprovalActorManager,
    action: ui.docApprovalActionApprove,
    tone: 'amber',
    imNode: 'node_doc_review',
  },
  {
    key: 'final',
    num: '3',
    title: ui.docApprovalStepFinal,
    actor: ui.docApprovalActorAdmin,
    action: ui.docApprovalActionApprove,
    tone: 'amber',
    imNode: 'node_doc_final',
  },
  {
    key: 'complete',
    num: '4',
    title: ui.docApprovalStepComplete,
    actor: '—',
    action: null,
    tone: 'green',
    imNode: null,
  },
] as const

/** 資料承認ワークフロー図 */
export function DocumentApprovalWorkflowDiagram() {
  return (
    <section className="saas-panel doc-approval-workflow" aria-label={ui.docApprovalWorkflowTitle}>
      <h2>{ui.docApprovalWorkflowTitle}</h2>
      <p className="muted small doc-approval-workflow-desc">{ui.docApprovalWorkflowDesc}</p>

      <div className="wf-diagram">
        <div className="wf-steps">
          {steps.map((step, index) => (
            <div key={step.key} className="wf-step-group">
              <div className={`wf-step wf-step--${step.tone}`}>
                <span className="wf-step-num">{step.num}</span>
                <div className="wf-step-body">
                  <strong>{step.title}</strong>
                  <span className="wf-step-actor">{step.actor}</span>
                  {step.action ? <span className="wf-step-action">{step.action}</span> : null}
                  {step.imNode ? <span className="wf-step-im">IM: {step.imNode}</span> : null}
                </div>
              </div>
              {index < steps.length - 1 ? (
                <span className="wf-arrow" aria-hidden>
                  →
                </span>
              ) : null}
            </div>
          ))}
        </div>

        <div className="wf-branches">
          <div className="wf-branch wf-branch--reject">
            <span className="wf-branch-label">{ui.docApprovalActionReject}</span>
            <span className="wf-branch-target">→ {ui.docApprovalStatusRejected}</span>
          </div>
          <div className="wf-branch wf-branch--return">
            <span className="wf-branch-label">{ui.docApprovalActionReturn}</span>
            <span className="wf-branch-target">→ {ui.docApprovalStepReview}</span>
          </div>
          <div className="wf-branch wf-branch--cancel">
            <span className="wf-branch-label">{ui.docApprovalActionCancel}</span>
            <span className="wf-branch-target">→ {ui.docApprovalStatusCancelled}</span>
          </div>
        </div>
      </div>
    </section>
  )
}
