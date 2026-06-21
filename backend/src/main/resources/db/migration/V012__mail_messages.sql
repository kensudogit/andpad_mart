-- 送信メールメッセージ（ワークフロー通知等）

CREATE TABLE mail_messages (
    id TEXT PRIMARY KEY,
    org_id TEXT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    mail_id TEXT,
    locale_id TEXT,
    recipients TEXT NOT NULL DEFAULT '',
    cc TEXT NOT NULL DEFAULT '',
    subject TEXT NOT NULL,
    body TEXT NOT NULL,
    parameters JSONB NOT NULL DEFAULT '{}',
    entity_type TEXT,
    entity_id TEXT,
    flow_id TEXT,
    sent BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_mail_messages_org_created ON mail_messages(org_id, created_at DESC);
CREATE INDEX idx_mail_messages_entity ON mail_messages(org_id, entity_type, entity_id);
