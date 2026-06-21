package jp.andpad.api.seed;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jp.andpad.api.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 組織ごとの建設 SaaS サンプルデータ投入。
 *
 * <p>デモ組織（{@code org_demo}）および新規登録組織向けに、各モジュールの表示用レコードを作成する。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrgSampleDataSeeder {

    private final JdbcTemplate jdbc;

    /** デモ組織向けサンプルデータを投入する。 */
    public void seedDemoOrg() {
        seedForOrg(TenantContext.DEMO_ORG_ID);
    }

    /** 案件データが無い組織へサンプルを補完する。 */
    public void seedOrgsMissingSampleData() {
        for (String orgId : jdbc.queryForList("SELECT id FROM organizations", String.class)) {
            Integer projectCount = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM construction_projects WHERE org_id = ?", Integer.class, orgId);
            if (projectCount == null || projectCount == 0) {
                seedForOrg(orgId);
            }
        }
    }

    /** 指定組織向けサンプルデータを投入する（既存データは保持）。 */
    public void seedForOrg(String orgId) {
        if (orgId == null || orgId.isBlank()) {
            return;
        }
        try {
            ensureProjects(orgId);
            ensureModuleRecords(orgId);
            ensureExtendedSamples(orgId);
            if (TenantContext.DEMO_ORG_ID.equals(orgId)) {
                ensureDemoOnlyArtifacts(orgId);
            }
            log.info("sample data seeded for org {}", orgId);
        } catch (Exception ex) {
            log.warn("sample data seed failed for org {}: {}", orgId, ex.getMessage());
        }
    }

    private void ensureProjects(String orgId) {
        String p1 = projectId(orgId, 1);
        String p2 = projectId(orgId, 2);
        jdbc.update(
                """
                INSERT INTO construction_projects (id, org_id, name, site_address, status, manager_name, start_date, end_date)
                VALUES
                    (?, ?, '渋谷オフィスビル新築工事', '東京都渋谷区道1-1-1', 'IN_PROGRESS', '山田 太郎',
                            CURRENT_DATE - 30, CURRENT_DATE + 180),
                    (?, ?, '横浜物流センター改修', '神奈川県横浜市西区1-2-3', 'PLANNING', '佐藤 花子',
                            CURRENT_DATE + 14, CURRENT_DATE + 365)
                ON CONFLICT (id) DO NOTHING
                """,
                p1,
                orgId,
                p2,
                orgId);
    }

    private void ensureModuleRecords(String orgId) {
        String p1 = projectId(orgId, 1);
        String p2 = projectId(orgId, 2);
        jdbc.update(
                """
                INSERT INTO project_module_records (id, org_id, project_id, module_code, title, status, detail,
                    amount, person_name, record_date)
                VALUES
                  (?, ?, ?, 'CONSTRUCTION_MGMT', '基礎工程進捗確認', 'IN_PROGRESS',
                   '配筋検査完了、次回コンクリート打設予定', NULL, '山田 太郎', CURRENT_DATE),
                  (?, ?, ?, 'DRAWINGS', '構造図 S-101 rev.3', 'APPROVED',
                   'RC造3階スラブ配筋図・最新版を現場共有', NULL, '佐藤 花子', CURRENT_DATE - 5),
                  (?, ?, ?, 'BLACKBOARD', '3階スラブ打設前確認', 'DONE',
                   '黒板付き写真を現場記録に登録', NULL, '山田 太郎', CURRENT_DATE - 2),
                  (?, ?, ?, 'INSPECTION', '配筋検査 3階スラブ', 'APPROVED',
                   '品質検査合格・写真添付済', NULL, '検査 一郎', CURRENT_DATE - 1),
                  (?, ?, ?, 'PROJECT_BOARD', '安全パトロール実施', 'IN_PROGRESS',
                   '来週火曜に全社パトロールを実施', NULL, '安全 太郎', CURRENT_DATE),
                  (?, ?, ?, 'INQUIRY_PROFIT', '本工事確定見積', 'WON',
                   '請負48.5億円・実行予算46.8億円で粗利確保', NULL, '山田 太郎', CURRENT_DATE - 45),
                  (?, ?, ?, 'ORDERS', '鉄骨加工発注', 'ORDERED',
                   '4階H形鋼 450×200 加工発注', 12800000, '調達 次郎', CURRENT_DATE - 10),
                  (?, ?, ?, 'REMOTE_SITE', '遠隔臨場 配筋確認', 'DONE',
                   '360°カメラで遠隔確認完了', NULL, '監理 三郎', CURRENT_DATE - 3),
                  (?, ?, ?, 'DOC_APPROVAL', '安全書類提出', 'IN_PROGRESS',
                   '書類承認ワークフロー申請中', NULL, '山田 太郎', CURRENT_DATE),
                  (?, ?, ?, 'SCAN_3D', 'B1F 3Dスキャン', 'DONE',
                   '点群データ登録済・BIM連携可能', NULL, '測量 四郎', CURRENT_DATE - 7),
                  (?, ?, ?, 'BILLING', '第1回出来高請求', 'PAID',
                   '完了出来高請求・入金確認済', 485000000, '山田 太郎', CURRENT_DATE - 60),
                  (?, ?, ?, 'BILLING', '第2回出来高請求', 'INVOICED',
                   '進捗出来高請求書発行済', 495000000, '山田 太郎', CURRENT_DATE - 30),
                  (?, ?, ?, 'WORK_RATE', '内装工事歩掛', 'OPEN',
                   '㎡あたり0.85人工の標準歩掛', 8500, '積算 五郎', CURRENT_DATE - 14),
                  (?, ?, ?, 'SITE_ACCESS', '協力会社入場 15名', 'DONE',
                   'ゲート通過記録・退場確認済', NULL, '警備 六郎', CURRENT_DATE),
                  (?, ?, ?, 'E_DELIVERY', '竣工図書電子納品', 'SUBMITTED',
                   '検査機関への電子納品データ作成', NULL, '技術 七郎', CURRENT_DATE - 20),
                  (?, ?, ?, 'BM', '空調設備定期点検', 'SCHEDULED',
                   '次回点検 8月15日予定', NULL, '設備 八郎', CURRENT_DATE + 30),
                  (?, ?, ?, 'ANALYTICS', '月次コストレポート', 'DONE',
                   '5月分の原価・進捗分析を確認', NULL, '経営 九郎', CURRENT_DATE - 15),
                  (?, ?, ?, 'DRAWINGS', '改修平面図 A-001', 'OPEN',
                   '横浜物流センター1階改修図面', NULL, '佐藤 花子', CURRENT_DATE - 3),
                  (?, ?, ?, 'ORDERS', '内装資材見積依頼', 'OPEN',
                   '床材・天井材の見積比較', 3200000, '調達 次郎', CURRENT_DATE)
                ON CONFLICT (id) DO NOTHING
                """,
                recordId(orgId, "construction"),
                orgId,
                p1,
                recordId(orgId, "drawings"),
                orgId,
                p1,
                recordId(orgId, "blackboard"),
                orgId,
                p1,
                recordId(orgId, "inspection"),
                orgId,
                p1,
                recordId(orgId, "board"),
                orgId,
                p1,
                recordId(orgId, "inquiry"),
                orgId,
                p1,
                recordId(orgId, "orders"),
                orgId,
                p1,
                recordId(orgId, "remote"),
                orgId,
                p1,
                recordId(orgId, "doc"),
                orgId,
                p1,
                recordId(orgId, "scan"),
                orgId,
                p1,
                recordId(orgId, "bill1"),
                orgId,
                p1,
                recordId(orgId, "bill2"),
                orgId,
                p1,
                recordId(orgId, "workrate"),
                orgId,
                p1,
                recordId(orgId, "access"),
                orgId,
                p1,
                recordId(orgId, "edelivery"),
                orgId,
                p1,
                recordId(orgId, "bm"),
                orgId,
                p1,
                recordId(orgId, "analytics"),
                orgId,
                p1,
                recordId(orgId, "drawings2"),
                orgId,
                p2,
                recordId(orgId, "orders2"),
                orgId,
                p2);
    }

    private void ensureExtendedSamples(String orgId) {
        String p1 = projectId(orgId, 1);
        String p2 = projectId(orgId, 2);
        jdbc.update(
                """
                INSERT INTO api_integrations (id, org_id, name, provider, endpoint_url, api_key_hint, status, last_sync_at)
                VALUES
                  (?, ?, 'kintone 案件連携', 'kintone', 'https://example.cybozu.com/k/v1/', '****7a3f', 'ACTIVE', NOW()),
                  (?, ?, 'freee 会計連携', 'freee', 'https://api.freee.co.jp/api/1/', '****9b2c', 'ACTIVE', NOW() - INTERVAL '6 hours')
                ON CONFLICT (id) DO NOTHING
                """,
                extId(orgId, "api1"),
                orgId,
                extId(orgId, "api2"),
                orgId);
        jdbc.update(
                """
                INSERT INTO bim_models (id, org_id, project_id, title, format, viewer_url, file_size_mb, status, uploaded_by)
                VALUES
                  (?, ?, ?, '本館構造BIMモデル v2', 'glTF',
                          'https://modelviewer.dev/shared-assets/models/Astronaut.glb', 128.5, 'READY', '山田 太郎'),
                  (?, ?, ?, '改修計画BIM', 'glTF',
                          'https://modelviewer.dev/shared-assets/models/Astronaut.glb', 42.0, 'PROCESSING', '佐藤 花子')
                ON CONFLICT (id) DO NOTHING
                """,
                extId(orgId, "bim1"),
                orgId,
                p1,
                extId(orgId, "bim2"),
                orgId,
                p2);
    }

    private void ensureDemoOnlyArtifacts(String orgId) {
        jdbc.update(
                """
                INSERT INTO wf_monitoring_flow_data (
                    id, org_id, flow_id, flow_name,
                    approve_count, approve_end_count, deny_count, discontinue_count, matter_handle_count,
                    minimum_time, maximum_time, average_time, amount_time, count_sum, updated_at
                )
                VALUES (
                    'mon-doc-demo', ?, 'document-approval', '書類承認',
                    '3', '2', '1', '1', '2',
                    '15', '120', '45', '135', '9', NOW()
                )
                ON CONFLICT (org_id, flow_id) DO UPDATE SET
                    flow_name = EXCLUDED.flow_name,
                    approve_count = EXCLUDED.approve_count,
                    approve_end_count = EXCLUDED.approve_end_count,
                    updated_at = NOW()
                """,
                orgId);
        jdbc.update(
                """
                INSERT INTO mail_messages (
                    id, org_id, mail_id, locale_id, recipients, cc, subject, body,
                    parameters, entity_type, entity_id, flow_id, sent, created_at
                )
                VALUES (
                    'mail-doc-demo-1', ?, 'andpad-doc-submit', 'ja', 'dev@andpad.local', '',
                    '[ANDPAD] 資料承認申請: 安全書類提出',
                    '山田 太郎 様\n\n資料「安全書類提出」のワークフローが更新されました。',
                    '{"title":"安全書類提出","flowId":"document-approval","entityId":"rec-doc-1"}'::jsonb,
                    'DOCUMENT', ?, 'document-approval', TRUE, NOW() - INTERVAL '1 hour'
                )
                ON CONFLICT (id) DO NOTHING
                """,
                orgId,
                recordId(orgId, "doc"));
        jdbc.update(
                """
                INSERT INTO wf_matter_stamps (
                    id, org_id, system_matter_id, stamp_no, node_id, process_date, process_id,
                    stamp_str1, stamp_str1_type, stamp_str2, stamp_str2_type, stamp_str3, stamp_str3_type,
                    stamp_type, cancel_flag, flow_id, entity_type, entity_id, workflow_instance_id, created_at
                )
                VALUES (
                    'stamp-doc-demo-1', ?, 'im-matter-doc-demo-1', '1', 'final', '2026/06/08 10:30:00', 'APPROVE',
                    '山田 太郎', 'user', '最終承認', 'node', 'approveEnd', 'type', '0',
                    'document-approval', 'DOCUMENT', ?, 'wf-doc-demo-1', NOW() - INTERVAL '30 minutes'
                )
                ON CONFLICT (id) DO NOTHING
                """,
                orgId,
                recordId(orgId, "doc"));
    }

    static String projectId(String orgId, int index) {
        if (TenantContext.DEMO_ORG_ID.equals(orgId)) {
            return index == 1 ? "prj-demo-1" : "prj-demo-2";
        }
        return orgId + "-prj-" + index;
    }

    static String recordId(String orgId, String suffix) {
        if (TenantContext.DEMO_ORG_ID.equals(orgId)) {
            return switch (suffix) {
                case "construction" -> "rec-demo-1";
                case "drawings" -> "rec-draw-1";
                case "blackboard" -> "rec-bb-1";
                case "inspection" -> "rec-insp-1";
                case "board" -> "rec-board-1";
                case "inquiry" -> "rec-inq-1";
                case "orders" -> "rec-ord-1";
                case "remote" -> "rec-remote-1";
                case "doc" -> "rec-doc-1";
                case "scan" -> "rec-scan-1";
                case "bill1" -> "rec-bill-1";
                case "bill2" -> "rec-bill-2";
                case "workrate" -> "rec-wr-1";
                case "access" -> "rec-access-1";
                case "edelivery" -> "rec-edel-1";
                case "bm" -> "rec-bm-1";
                case "analytics" -> "rec-analytics-1";
                case "drawings2" -> "rec-draw-2";
                case "orders2" -> "rec-ord-2";
                default -> "rec-" + suffix + "-demo";
            };
        }
        return orgId + "-rec-" + suffix;
    }

    private static String extId(String orgId, String suffix) {
        if (TenantContext.DEMO_ORG_ID.equals(orgId)) {
            return switch (suffix) {
                case "api1" -> "api-demo-1";
                case "api2" -> "api-demo-2";
                case "bim1" -> "bim-demo-1";
                case "bim2" -> "bim-demo-3";
                default -> suffix + "-demo";
            };
        }
        return orgId + "-" + suffix;
    }
}
