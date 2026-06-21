-- 完了案件印影データ（CplMatterStampList 連携）

CREATE TABLE wf_matter_stamps (
    id TEXT PRIMARY KEY,
    org_id TEXT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    system_matter_id TEXT NOT NULL,
    stamp_no TEXT,
    node_id TEXT,
    process_date TEXT,
    process_id TEXT,
    stamp_str1 TEXT,
    stamp_str1_type TEXT,
    stamp_str2 TEXT,
    stamp_str2_type TEXT,
    stamp_str3 TEXT,
    stamp_str3_type TEXT,
    stamp_type TEXT,
    cancel_flag TEXT,
    flow_id TEXT,
    entity_type TEXT,
    entity_id TEXT,
    workflow_instance_id TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_wf_matter_stamps_org ON wf_matter_stamps(org_id, flow_id);
CREATE INDEX idx_wf_matter_stamps_entity ON wf_matter_stamps(org_id, entity_type, entity_id);
CREATE INDEX idx_wf_matter_stamps_matter ON wf_matter_stamps(org_id, system_matter_id);
