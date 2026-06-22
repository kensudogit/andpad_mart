-- 未完了案件確認一覧（CnfmActvMatterList 連携）

CREATE TABLE wf_cnfm_actv_matters (
    id TEXT PRIMARY KEY,
    org_id TEXT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    list_type TEXT NOT NULL,
    system_matter_id TEXT NOT NULL,
    flow_id TEXT NOT NULL,
    flow_name TEXT,
    matter_name TEXT,
    matter_number TEXT,
    node_id TEXT,
    apply_auth_user_code TEXT,
    apply_auth_user_name TEXT,
    apply_date TEXT,
    arrived_date TEXT,
    confirm_cpl_flag TEXT NOT NULL DEFAULT '0',
    priority_level TEXT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (org_id, list_type, system_matter_id)
);

CREATE INDEX idx_wf_cnfm_actv_org_type ON wf_cnfm_actv_matters(org_id, list_type, updated_at DESC);
CREATE INDEX idx_wf_cnfm_actv_flow ON wf_cnfm_actv_matters(org_id, flow_id);
