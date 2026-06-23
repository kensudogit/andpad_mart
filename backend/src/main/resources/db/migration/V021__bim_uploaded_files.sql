-- BIM アップロードファイル（Railway 等のエフェメラルディスク対策）

CREATE TABLE bim_uploaded_files (
    id TEXT PRIMARY KEY,
    org_id TEXT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    stored_name TEXT NOT NULL,
    file_kind TEXT NOT NULL,
    content_type TEXT NOT NULL,
    data BYTEA NOT NULL,
    size_bytes BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (org_id, stored_name)
);

CREATE INDEX idx_bim_uploaded_files_org ON bim_uploaded_files(org_id, created_at DESC);
