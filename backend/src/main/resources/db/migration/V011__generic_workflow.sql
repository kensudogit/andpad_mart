-- 汎用ワークフロー（定義・インスタンス・タスク・履歴）

CREATE TABLE wf_definitions (
    id TEXT PRIMARY KEY,
    org_id TEXT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    flow_id TEXT NOT NULL,
    name TEXT NOT NULL,
    version INT NOT NULL DEFAULT 1,
    entity_type TEXT NOT NULL DEFAULT 'GENERIC',
    description TEXT NOT NULL DEFAULT '',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (org_id, flow_id, version)
);

CREATE TABLE wf_steps (
    id TEXT PRIMARY KEY,
    definition_id TEXT NOT NULL REFERENCES wf_definitions(id) ON DELETE CASCADE,
    step_key TEXT NOT NULL,
    name TEXT NOT NULL,
    step_order INT NOT NULL,
    step_type TEXT NOT NULL,
    assignee_type TEXT NOT NULL,
    assignee_value TEXT,
    im_node_id TEXT,
    UNIQUE (definition_id, step_key)
);

CREATE TABLE wf_instances (
    id TEXT PRIMARY KEY,
    org_id TEXT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    definition_id TEXT NOT NULL REFERENCES wf_definitions(id),
    entity_type TEXT NOT NULL,
    entity_id TEXT NOT NULL,
    status TEXT NOT NULL,
    current_step_key TEXT,
    title TEXT NOT NULL,
    payload JSONB NOT NULL DEFAULT '{}',
    submitter_user_id TEXT NOT NULL,
    im_system_matter_id TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMPTZ
);

CREATE INDEX idx_wf_instances_org_entity ON wf_instances(org_id, entity_type, entity_id);
CREATE INDEX idx_wf_instances_org_status ON wf_instances(org_id, status);

CREATE TABLE wf_tasks (
    id TEXT PRIMARY KEY,
    org_id TEXT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    instance_id TEXT NOT NULL REFERENCES wf_instances(id) ON DELETE CASCADE,
    step_key TEXT NOT NULL,
    assignee_type TEXT NOT NULL,
    assignee_value TEXT,
    status TEXT NOT NULL,
    action_taken TEXT,
    comment TEXT,
    acted_by_user_id TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMPTZ
);

CREATE INDEX idx_wf_tasks_instance ON wf_tasks(instance_id, status);
CREATE INDEX idx_wf_tasks_org_pending ON wf_tasks(org_id, status);

CREATE TABLE wf_history (
    id TEXT PRIMARY KEY,
    org_id TEXT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    instance_id TEXT NOT NULL REFERENCES wf_instances(id) ON DELETE CASCADE,
    step_key TEXT,
    action TEXT NOT NULL,
    actor_user_id TEXT NOT NULL,
    actor_name TEXT NOT NULL,
    comment TEXT,
    metadata JSONB NOT NULL DEFAULT '{}',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_wf_history_instance ON wf_history(instance_id, created_at);
