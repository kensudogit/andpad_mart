-- 非同期処理状況情報（AsyncProcessWorkflow 連携）

CREATE TABLE wf_async_process_status (
    id TEXT PRIMARY KEY,
    org_id TEXT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    accept_id TEXT NOT NULL,
    async_proc_status TEXT NOT NULL,
    auth_user_code TEXT,
    execute_user_code TEXT,
    flow_id TEXT NOT NULL,
    matter_name TEXT,
    matter_number TEXT,
    message TEXT,
    node_id TEXT,
    proc_comment TEXT,
    proc_date TEXT,
    proc_type TEXT,
    queue_id TEXT,
    sub_message TEXT,
    system_matter_id TEXT NOT NULL,
    entity_type TEXT,
    entity_id TEXT,
    workflow_instance_id TEXT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (org_id, accept_id)
);

CREATE INDEX idx_wf_async_process_org_flow ON wf_async_process_status(org_id, flow_id, updated_at DESC);
CREATE INDEX idx_wf_async_process_system_matter ON wf_async_process_status(system_matter_id);
