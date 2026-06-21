-- フロー別モニタリング情報（MonitoringManager 連携）

CREATE TABLE wf_monitoring_flow_data (
    id TEXT PRIMARY KEY,
    org_id TEXT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    flow_id TEXT NOT NULL,
    flow_name TEXT,
    approve_count TEXT NOT NULL DEFAULT '1',
    approve_end_count TEXT NOT NULL DEFAULT '1',
    deny_count TEXT NOT NULL DEFAULT '1',
    discontinue_count TEXT NOT NULL DEFAULT '1',
    matter_handle_count TEXT NOT NULL DEFAULT '1',
    minimum_time TEXT NOT NULL DEFAULT '1',
    maximum_time TEXT NOT NULL DEFAULT '1',
    average_time TEXT NOT NULL DEFAULT '1',
    amount_time TEXT NOT NULL DEFAULT '1',
    count_sum TEXT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (org_id, flow_id)
);

CREATE INDEX idx_wf_monitoring_flow_org ON wf_monitoring_flow_data(org_id, flow_id);
