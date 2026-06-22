-- 新規テナント作成申請（テナント管理）

ALTER TABLE organizations
    ADD COLUMN IF NOT EXISTS address TEXT,
    ADD COLUMN IF NOT EXISTS contact_name TEXT,
    ADD COLUMN IF NOT EXISTS contact_email TEXT,
    ADD COLUMN IF NOT EXISTS contact_phone TEXT;

CREATE TABLE tenant_applications (
    id TEXT PRIMARY KEY,
    applicant_org_id TEXT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    applicant_user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    slug TEXT NOT NULL,
    address TEXT,
    contact_name TEXT NOT NULL,
    contact_email TEXT NOT NULL,
    contact_phone TEXT,
    owner_name TEXT NOT NULL,
    owner_email TEXT NOT NULL,
    notes TEXT,
    status TEXT NOT NULL DEFAULT 'DRAFT',
    workflow_instance_id TEXT,
    created_org_id TEXT REFERENCES organizations(id),
    submitted_at TIMESTAMPTZ,
    approved_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_tenant_applications_org ON tenant_applications(applicant_org_id, status);
CREATE INDEX idx_tenant_applications_status ON tenant_applications(status, created_at DESC);

CREATE TABLE tenant_application_documents (
    id TEXT PRIMARY KEY,
    application_id TEXT NOT NULL REFERENCES tenant_applications(id) ON DELETE CASCADE,
    file_name TEXT NOT NULL,
    content_type TEXT,
    content_text TEXT,
    uploaded_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_tenant_application_docs_app ON tenant_application_documents(application_id);
