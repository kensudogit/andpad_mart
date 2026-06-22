'use client'

/**
 * テナント管理 — 新規テナント作成の6ステップウィザードと承認 UI。
 */
import Link from 'next/link'
import { useMutation, useQuery } from '@apollo/client/react'
import { useCallback, useEffect, useMemo, useState } from 'react'
import {
  CompleteTenantApprovalTaskDocument,
  CreateTenantApplicationDocument,
  CurrentSessionDocument,
  SubmitTenantApplicationDocument,
  TenantApplicationDetailDocument,
  TenantApplicationsDocument,
  TenantApprovalTasksDocument,
  TenantApplicationStatus,
  UpdateTenantApplicationDocument,
  UploadTenantApplicationDocumentDocument,
  WorkflowAction,
} from '@/lib/generated/graphql'
import { isAuthRequiredGraphQLError, isNetworkGraphQLError } from '@/lib/graphql-errors'
import { ui } from '@/lib/ui'

const STEPS = [
  ui.tenantStepLogin,
  ui.tenantStepNavigate,
  ui.tenantStepCreate,
  ui.tenantStepConfirm,
  ui.tenantStepDocuments,
  ui.tenantStepApproval,
] as const

type FormState = {
  name: string
  slug: string
  address: string
  contactName: string
  contactEmail: string
  contactPhone: string
  ownerName: string
  ownerEmail: string
  notes: string
}

const emptyForm: FormState = {
  name: '',
  slug: '',
  address: '',
  contactName: '',
  contactEmail: '',
  contactPhone: '',
  ownerName: '',
  ownerEmail: '',
  notes: '',
}

function statusLabel(status: TenantApplicationStatus) {
  switch (status) {
    case TenantApplicationStatus.Draft:
      return ui.tenantStatusDraft
    case TenantApplicationStatus.PendingApproval:
      return ui.tenantStatusPending
    case TenantApplicationStatus.Active:
      return ui.tenantStatusActive
    case TenantApplicationStatus.Rejected:
      return ui.tenantStatusRejected
    default:
      return status
  }
}

function formFromApp(app: {
  name: string
  slug: string
  address?: string | null
  contactName: string
  contactEmail: string
  contactPhone?: string | null
  ownerName: string
  ownerEmail: string
  notes?: string | null
}): FormState {
  return {
    name: app.name,
    slug: app.slug,
    address: app.address ?? '',
    contactName: app.contactName,
    contactEmail: app.contactEmail,
    contactPhone: app.contactPhone ?? '',
    ownerName: app.ownerName,
    ownerEmail: app.ownerEmail,
    notes: app.notes ?? '',
  }
}

/** テナント管理メイン UI */
export function TenantManagementClient() {
  const [wizardOpen, setWizardOpen] = useState(false)
  const [step, setStep] = useState(0)
  const [editId, setEditId] = useState<string | null>(null)
  const [form, setForm] = useState<FormState>(emptyForm)
  const [docFileName, setDocFileName] = useState('')
  const [docContent, setDocContent] = useState('')
  const [approvalComment, setApprovalComment] = useState('')
  const [message, setMessage] = useState<string | null>(null)

  const { data: sessionData, loading: sessionLoading } = useQuery(CurrentSessionDocument, {
    fetchPolicy: 'network-only',
  })
  const session = sessionData?.currentSession

  const {
    data: listData,
    loading: listLoading,
    error: listError,
    refetch: refetchList,
  } = useQuery(TenantApplicationsDocument, {
    skip: !session,
    fetchPolicy: 'network-only',
  })

  const { data: detailData, refetch: refetchDetail } = useQuery(TenantApplicationDetailDocument, {
    skip: !editId || !session,
    variables: { id: editId ?? '' },
    fetchPolicy: 'network-only',
  })

  const { data: approvalData, refetch: refetchApproval } = useQuery(TenantApprovalTasksDocument, {
    skip: !session || !listData?.isPlatformTenantAdmin,
    fetchPolicy: 'network-only',
  })

  const [createApp, { loading: creating }] = useMutation(CreateTenantApplicationDocument)
  const [updateApp, { loading: updating }] = useMutation(UpdateTenantApplicationDocument)
  const [uploadDoc, { loading: uploading }] = useMutation(UploadTenantApplicationDocumentDocument)
  const [submitApp, { loading: submitting }] = useMutation(SubmitTenantApplicationDocument)
  const [completeTask, { loading: completing }] = useMutation(CompleteTenantApprovalTaskDocument)

  const currentApp = detailData?.tenantApplication ?? null
  const applications = listData?.tenantApplications ?? []
  const isPlatformAdmin = listData?.isPlatformTenantAdmin ?? false

  const tenantTasks = useMemo(() => {
    const appsByInstance = new Map(
      applications
        .filter((a) => a.workflowInstanceId)
        .map((a) => [a.workflowInstanceId!, a]),
    )
    return (approvalData?.myWorkflowTasks ?? [])
      .filter((t) => appsByInstance.has(t.instanceId))
      .map((t) => ({ task: t, app: appsByInstance.get(t.instanceId)! }))
  }, [approvalData?.myWorkflowTasks, applications])

  const startNew = useCallback(() => {
    setEditId(null)
    setForm(emptyForm)
    setStep(session ? 1 : 0)
    setWizardOpen(true)
    setMessage(null)
  }, [session])

  const openDraftWithApp = useCallback(
    (app: (typeof applications)[number]) => {
      setEditId(app.id)
      setForm(formFromApp(app))
      setStep(app.status === TenantApplicationStatus.Draft ? 2 : 5)
      setWizardOpen(true)
      setMessage(null)
    },
    [],
  )

  useEffect(() => {
    if (editId && currentApp && wizardOpen) {
      setForm(formFromApp(currentApp))
    }
  }, [editId, currentApp, wizardOpen])

  async function saveDraft() {
    setMessage(null)
    const input = {
      name: form.name.trim(),
      slug: form.slug.trim() || undefined,
      address: form.address.trim() || undefined,
      contactName: form.contactName.trim(),
      contactEmail: form.contactEmail.trim(),
      contactPhone: form.contactPhone.trim() || undefined,
      ownerName: form.ownerName.trim(),
      ownerEmail: form.ownerEmail.trim(),
      notes: form.notes.trim() || undefined,
    }
    try {
      if (editId) {
        await updateApp({ variables: { input: { id: editId, ...input } } })
        await refetchDetail()
      } else {
        const res = await createApp({ variables: { input } })
        const id = res.data?.createTenantApplication.id
        if (id) {
          setEditId(id)
        }
      }
      await refetchList()
      setMessage('下書きを保存しました')
      setStep(4)
    } catch (err) {
      setMessage(err instanceof Error ? err.message : ui.saveFailed)
    }
  }

  async function addDocument() {
    if (!editId || !docFileName.trim()) return
    setMessage(null)
    try {
      await uploadDoc({
        variables: {
          input: {
            applicationId: editId,
            fileName: docFileName.trim(),
            contentType: 'text/plain',
            contentText: docContent.trim() || undefined,
          },
        },
      })
      setDocFileName('')
      setDocContent('')
      await refetchDetail()
      await refetchList()
      setMessage('書類を追加しました')
    } catch (err) {
      setMessage(err instanceof Error ? err.message : ui.saveFailed)
    }
  }

  async function submitForApproval() {
    if (!editId) return
    setMessage(null)
    try {
      await submitApp({ variables: { id: editId } })
      await refetchList()
      await refetchDetail()
      await refetchApproval()
      setStep(5)
      setMessage('承認申請を送信しました')
    } catch (err) {
      setMessage(err instanceof Error ? err.message : ui.saveFailed)
    }
  }

  async function handleApproval(taskId: string, action: WorkflowAction) {
    setMessage(null)
    try {
      await completeTask({
        variables: {
          input: {
            taskId,
            action,
            comment: approvalComment.trim() || undefined,
          },
        },
      })
      setApprovalComment('')
      await refetchList()
      await refetchApproval()
      setMessage(action === WorkflowAction.Approve ? '承認しました' : '却下しました')
    } catch (err) {
      setMessage(err instanceof Error ? err.message : ui.saveFailed)
    }
  }

  if (sessionLoading) {
    return <p className="muted">{ui.settingsLoading}</p>
  }

  const authRequired = !session
  const apiFailed = listError && isNetworkGraphQLError(listError)

  return (
    <>
      <div className="page-head">
        <h1>{ui.tenantTitle}</h1>
        <p>{ui.tenantDesc}</p>
      </div>

      {apiFailed ? (
        <div className="panel">
          <p className="alert">{listError.message}</p>
          <Link href="/status">/status</Link>
        </div>
      ) : null}

      {!wizardOpen ? (
        <>
          <div className="form-actions" style={{ borderTop: 'none', paddingTop: 0, marginBottom: '1rem' }}>
            <button type="button" className="btn" onClick={startNew} disabled={authRequired}>
              {ui.tenantNew}
            </button>
          </div>

          {authRequired ? (
            <div className="panel">
              <p>{ui.settingsSignIn}</p>
              <p className="muted small">{ui.tenantLoginHint}</p>
              <Link href="/login" className="btn">
                {ui.loginSubmit}
              </Link>
            </div>
          ) : listLoading ? (
            <p className="muted">{ui.settingsLoading}</p>
          ) : listError && isAuthRequiredGraphQLError(listError) ? (
            <div className="panel">
              <p>{ui.settingsSignIn}</p>
              <Link href="/login" className="btn">
                {ui.loginSubmit}
              </Link>
            </div>
          ) : (
            <>
              <section className="panel">
                <h3>{ui.tenantListTitle}</h3>
                {applications.length === 0 ? (
                  <p className="muted">{ui.tenantListEmpty}</p>
                ) : (
                  <ul className="metric-list">
                    {applications.map((app) => (
                      <li key={app.id}>
                        <span>
                          {app.name} — {statusLabel(app.status)}
                          {app.createdOrgId ? ` (${app.createdOrgId})` : ''}
                        </span>
                        {app.status === TenantApplicationStatus.Draft ? (
                          <button
                            type="button"
                            className="btn btn-outline btn-sm"
                            onClick={() => openDraftWithApp(app)}
                          >
                            {ui.tenantContinue}
                          </button>
                        ) : null}
                      </li>
                    ))}
                  </ul>
                )}
              </section>

              {isPlatformAdmin ? (
                <section className="panel" style={{ marginTop: '1rem' }}>
                  <h3>{ui.tenantApprovalTitle}</h3>
                  {tenantTasks.length === 0 ? (
                    <p className="muted">{ui.tenantApprovalEmpty}</p>
                  ) : (
                    <ul className="metric-list">
                      {tenantTasks.map(({ task, app }) => (
                        <li key={task.id} className="tenant-approval-row">
                          <div>
                            <strong>{app.name}</strong>
                            <span className="muted small">
                              {' '}
                              — {task.stepName ?? task.stepKey}
                            </span>
                          </div>
                          <label className="tenant-approval-comment">
                            {ui.tenantApprovalComment}
                            <input
                              value={approvalComment}
                              onChange={(e) => setApprovalComment(e.target.value)}
                              placeholder="OK"
                            />
                          </label>
                          <div className="tenant-approval-actions">
                            <button
                              type="button"
                              className="btn btn-sm"
                              disabled={completing}
                              onClick={() => handleApproval(task.id, WorkflowAction.Approve)}
                            >
                              {ui.tenantApprove}
                            </button>
                            <button
                              type="button"
                              className="btn btn-outline btn-sm"
                              disabled={completing}
                              onClick={() => handleApproval(task.id, WorkflowAction.Reject)}
                            >
                              {ui.tenantReject}
                            </button>
                          </div>
                        </li>
                      ))}
                    </ul>
                  )}
                </section>
              ) : null}
            </>
          )}
        </>
      ) : (
        <div className="panel tenant-wizard">
          <nav className="tenant-steps" aria-label="tenant wizard steps">
            {STEPS.map((label, i) => (
              <span
                key={label}
                className={`tenant-step${i === step ? ' active' : i < step ? ' done' : ''}`}
              >
                {label}
              </span>
            ))}
          </nav>

          {message ? <p className="tenant-message">{message}</p> : null}

          {step === 0 ? (
            <section>
              <p>{ui.tenantLoginHint}</p>
              <p className="muted small">{ui.loginDemoHint}</p>
              <Link href="/login" className="btn">
                {ui.loginSubmit}
              </Link>
            </section>
          ) : null}

          {step === 1 ? (
            <section>
              <p>{ui.tenantNavigateHint}</p>
              <div className="form-actions">
                <button type="button" className="btn" onClick={() => setStep(2)}>
                  {ui.tenantNext}
                </button>
                <button type="button" className="btn btn-outline" onClick={() => setWizardOpen(false)}>
                  {ui.tenantBackList}
                </button>
              </div>
            </section>
          ) : null}

          {step === 2 ? (
            <section className="auth-form">
              <label>
                {ui.tenantName}
                <input
                  value={form.name}
                  onChange={(e) => setForm({ ...form, name: e.target.value })}
                  required
                />
              </label>
              <label>
                {ui.tenantSlug}
                <input
                  value={form.slug}
                  onChange={(e) => setForm({ ...form, slug: e.target.value })}
                  placeholder="auto-generated"
                />
              </label>
              <label>
                {ui.tenantAddress}
                <input
                  value={form.address}
                  onChange={(e) => setForm({ ...form, address: e.target.value })}
                />
              </label>
              <label>
                {ui.tenantContactName}
                <input
                  value={form.contactName}
                  onChange={(e) => setForm({ ...form, contactName: e.target.value })}
                  required
                />
              </label>
              <label>
                {ui.tenantContactEmail}
                <input
                  type="email"
                  value={form.contactEmail}
                  onChange={(e) => setForm({ ...form, contactEmail: e.target.value })}
                  required
                />
              </label>
              <label>
                {ui.tenantContactPhone}
                <input
                  value={form.contactPhone}
                  onChange={(e) => setForm({ ...form, contactPhone: e.target.value })}
                />
              </label>
              <label>
                {ui.tenantOwnerName}
                <input
                  value={form.ownerName}
                  onChange={(e) => setForm({ ...form, ownerName: e.target.value })}
                  required
                />
              </label>
              <label>
                {ui.tenantOwnerEmail}
                <input
                  type="email"
                  value={form.ownerEmail}
                  onChange={(e) => setForm({ ...form, ownerEmail: e.target.value })}
                  required
                />
              </label>
              <label>
                {ui.tenantNotes}
                <textarea
                  value={form.notes}
                  onChange={(e) => setForm({ ...form, notes: e.target.value })}
                  rows={3}
                />
              </label>
              <div className="form-actions">
                <button type="button" className="btn btn-outline" onClick={() => setStep(1)}>
                  {ui.tenantPrev}
                </button>
                <button
                  type="button"
                  className="btn"
                  onClick={() => setStep(3)}
                  disabled={
                    !form.name.trim() ||
                    !form.contactName.trim() ||
                    !form.contactEmail.trim() ||
                    !form.ownerName.trim() ||
                    !form.ownerEmail.trim()
                  }
                >
                  {ui.tenantNext}
                </button>
              </div>
            </section>
          ) : null}

          {step === 3 ? (
            <section>
              <p>{ui.tenantConfirmHint}</p>
              <dl className="tenant-summary">
                <dt>{ui.tenantName}</dt>
                <dd>{form.name}</dd>
                <dt>{ui.tenantSlug}</dt>
                <dd>{form.slug || '（自動生成）'}</dd>
                <dt>{ui.tenantAddress}</dt>
                <dd>{form.address || '—'}</dd>
                <dt>{ui.tenantContactName}</dt>
                <dd>{form.contactName}</dd>
                <dt>{ui.tenantContactEmail}</dt>
                <dd>{form.contactEmail}</dd>
                <dt>{ui.tenantOwnerName}</dt>
                <dd>{form.ownerName}</dd>
              </dl>
              <div className="form-actions">
                <button type="button" className="btn btn-outline" onClick={() => setStep(2)}>
                  {ui.tenantPrev}
                </button>
                <button
                  type="button"
                  className="btn"
                  onClick={saveDraft}
                  disabled={creating || updating}
                >
                  {creating || updating ? ui.tenantSaving : ui.tenantSaveDraft}
                </button>
              </div>
            </section>
          ) : null}

          {step === 4 ? (
            <section className="auth-form">
              <h4>{ui.tenantDocList}</h4>
              {(currentApp?.documents ?? []).length === 0 ? (
                <p className="muted">{ui.tenantDocEmpty}</p>
              ) : (
                <ul className="metric-list">
                  {(currentApp?.documents ?? []).map((doc) => (
                    <li key={doc.id}>
                      {doc.fileName}
                      <span className="muted small"> — {doc.uploadedAt.slice(0, 10)}</span>
                    </li>
                  ))}
                </ul>
              )}
              <label>
                {ui.tenantDocFileName}
                <input value={docFileName} onChange={(e) => setDocFileName(e.target.value)} />
              </label>
              <label>
                {ui.tenantDocContent}
                <textarea
                  value={docContent}
                  onChange={(e) => setDocContent(e.target.value)}
                  rows={4}
                  placeholder="登記簿謄本、契約書などの内容メモ"
                />
              </label>
              <div className="form-actions">
                <button type="button" className="btn btn-outline" onClick={() => setStep(3)}>
                  {ui.tenantPrev}
                </button>
                <button
                  type="button"
                  className="btn"
                  onClick={addDocument}
                  disabled={uploading || !docFileName.trim() || !editId}
                >
                  {uploading ? ui.tenantSaving : ui.tenantDocUpload}
                </button>
                <button type="button" className="btn btn-outline" onClick={() => setStep(5)}>
                  {ui.tenantNext}
                </button>
              </div>
            </section>
          ) : null}

          {step === 5 ? (
            <section>
              {currentApp?.status === TenantApplicationStatus.Draft ? (
                <>
                  <p>{ui.tenantSubmitHint}</p>
                  <div className="form-actions">
                    <button type="button" className="btn btn-outline" onClick={() => setStep(4)}>
                      {ui.tenantPrev}
                    </button>
                    <button
                      type="button"
                      className="btn"
                      onClick={submitForApproval}
                      disabled={submitting || !editId}
                    >
                      {submitting ? ui.tenantSubmitting : ui.tenantSubmit}
                    </button>
                  </div>
                </>
              ) : (
                <>
                  <p>
                    <strong>{statusLabel(currentApp?.status ?? TenantApplicationStatus.Draft)}</strong>
                  </p>
                  {currentApp?.status === TenantApplicationStatus.PendingApproval ? (
                    <p className="muted">{ui.tenantApprovalPending}</p>
                  ) : null}
                  {currentApp?.createdOrgId ? (
                    <p>{ui.tenantCreatedOrg(currentApp.createdOrgId)}</p>
                  ) : null}
                  <div className="form-actions">
                    <button type="button" className="btn" onClick={() => setWizardOpen(false)}>
                      {ui.tenantBackList}
                    </button>
                  </div>
                </>
              )}
            </section>
          ) : null}
        </div>
      )}
    </>
  )
}
