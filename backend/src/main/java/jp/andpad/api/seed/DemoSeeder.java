package jp.andpad.api.seed;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jp.andpad.api.demo.DemoCatalog;
import jp.andpad.api.demo.DemoCatalog.CatalogInstructor;
import jp.andpad.api.demo.DemoCatalog.CatalogPath;
import jp.andpad.api.demo.DemoCatalog.CatalogVideo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** org_demo のデモデータ投入（Go 版 seed.go と同等の最小セット）。 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DemoSeeder {

    private static final String DEMO_EMAIL = "demo@sakura-dental.jp";
    private static final String DEMO_PASSWORD = "demo1234";

    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    public void seedDemo() {
        try {
            seedDemoTx();
        } catch (Exception ex) {
            log.error("Demo seed failed (API continues): {}", ex.getMessage(), ex);
        }
    }

    @Transactional
    void seedDemoTx() {
        ensureOrganization();
        ensureDemoUser();
        ensureLearningDemo();
        ensureConstructionDemo();
        ensureSaasBusinessDemo();
        ensureExtendedDemo();
        ensureBudgetDemo();
        ensureWorkflowDemo();
        ensureDocApprovalArtifactsDemo();
    }

    private void ensureOrganization() {
        jdbc.update(
                """
                INSERT INTO organizations (id, name, slug, plan_tier, subscription_status, seat_count, timezone)
                VALUES ('org_demo', 'サンプル建設株式会社', 'sample-construction', 'PRO', 'ACTIVE', 10, 'Asia/Tokyo')
                ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name
                """);
        jdbc.update(
                """
                INSERT INTO org_modules (org_id, module_code, enabled)
                SELECT 'org_demo', code, TRUE FROM saas_modules
                ON CONFLICT DO NOTHING
                """);
        jdbc.update(
                "INSERT INTO usage_counters (org_id) VALUES ('org_demo') ON CONFLICT (org_id) DO NOTHING");
    }

    private void ensureDemoUser() {
        String hash = passwordEncoder.encode(DEMO_PASSWORD);
        jdbc.update(
                """
                INSERT INTO users (id, email, name, password_hash)
                VALUES ('user_demo', ?, '田中 健一', ?)
                ON CONFLICT (id) DO UPDATE SET password_hash = EXCLUDED.password_hash, email = EXCLUDED.email
                """,
                DEMO_EMAIL,
                hash);
        jdbc.update(
                """
                INSERT INTO team_members (id, org_id, user_id, role)
                VALUES ('tm_demo', 'org_demo', 'user_demo', 'OWNER')
                ON CONFLICT (org_id, user_id) DO NOTHING
                """);
    }

    private void ensureLearningDemo() {
        for (CatalogInstructor inst : DemoCatalog.instructors()) {
            jdbc.update(
                    """
                    INSERT INTO instructors (id, org_id, name, title, specialty, bio, avatar_url)
                    VALUES (?, 'org_demo', ?, ?, ?, ?, ?)
                    ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, title = EXCLUDED.title
                    """,
                    inst.id(),
                    inst.name(),
                    inst.title(),
                    inst.specialty(),
                    inst.bio(),
                    "/avatars/" + inst.id() + ".svg");
        }
        for (CatalogVideo v : DemoCatalog.videos()) {
            jdbc.update(
                    """
                    INSERT INTO videos (id, org_id, instructor_id, title, description, category, procedure, skill_level,
                        duration_sec, thumbnail_url, video_url, featured, published_at)
                    VALUES (?, 'org_demo', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())
                    ON CONFLICT (id) DO UPDATE SET
                        title = EXCLUDED.title,
                        description = EXCLUDED.description,
                        thumbnail_url = EXCLUDED.thumbnail_url,
                        video_url = EXCLUDED.video_url,
                        featured = EXCLUDED.featured
                    """,
                    v.id(),
                    v.instructorId(),
                    v.title(),
                    v.description(),
                    v.category(),
                    v.procedure(),
                    v.skillLevel(),
                    v.durationSec(),
                    v.thumbnailUrl(),
                    v.embedUrl(),
                    v.featured());
        }
        for (CatalogPath path : DemoCatalog.paths()) {
            jdbc.update(
                    """
                    INSERT INTO learning_paths (id, org_id, title, description, category, skill_level,
                        estimated_minutes, enrolled_count, certificate_title)
                    VALUES (?, 'org_demo', ?, ?, ?, ?, ?, ?, ?)
                    ON CONFLICT (id) DO UPDATE SET title = EXCLUDED.title, description = EXCLUDED.description
                    """,
                    path.id(),
                    path.title(),
                    path.description(),
                    path.category(),
                    path.skillLevel(),
                    path.estimatedMinutes(),
                    path.enrolledCount(),
                    path.certificate());
            for (int i = 0; i < path.videoIds().size(); i++) {
                jdbc.update(
                        """
                        INSERT INTO path_videos (path_id, video_id, sort_order) VALUES (?, ?, ?)
                        ON CONFLICT (path_id, video_id) DO UPDATE SET sort_order = EXCLUDED.sort_order
                        """,
                        path.id(),
                        path.videoIds().get(i),
                        i + 1);
            }
        }
        ensureQuizDemo();
    }

    private void ensureQuizDemo() {
        jdbc.update(
                """
                INSERT INTO quizzes (id, org_id, video_id, title, passing_score)
                VALUES ('quiz-v1', 'org_demo', 'v-1', '根管治療 Step1 確認テスト', 70)
                ON CONFLICT (id) DO NOTHING
                """);
        jdbc.update(
                """
                INSERT INTO quiz_questions (id, quiz_id, prompt, correct_index, sort_order)
                VALUES
                  ('qq-v1-1', 'quiz-v1', '適切なアクセス窩形成の目的は？', 0, 1),
                  ('qq-v1-2', 'quiz-v1', '根管入口の確認で最も重要なのは？', 1, 2)
                ON CONFLICT (id) DO NOTHING
                """);
        jdbc.update(
                """
                INSERT INTO quiz_choices (id, question_id, label, sort_order)
                VALUES
                  ('qc-v1-1a', 'qq-v1-1', '根管治療器具の進入経路を確保する', 1),
                  ('qc-v1-1b', 'qq-v1-1', '審美修復のみを目的とする', 2),
                  ('qc-v1-1c', 'qq-v1-1', '歯肉切除を行う', 3),
                  ('qc-v1-2a', 'qq-v1-2', '速度優先で拡大する', 1),
                  ('qc-v1-2b', 'qq-v1-2', '根管口の位置と形状を正確に把握する', 2),
                  ('qc-v1-2c', 'qq-v1-2', '必ず歯髄を残す', 3)
                ON CONFLICT (id) DO NOTHING
                """);
        jdbc.update(
                """
                INSERT INTO rag_documents (id, org_id, title, content, tags)
                VALUES
                  ('rag-1', 'org_demo', '感染対策マニュアル',
                   '手洗いは20秒以上、アルコール消毒はドアノブとスイッチを使用。手袋は一回のための使用を原則とする。',
                   ARRAY['感染対策', '院内規程']),
                  ('rag-2', 'org_demo', '予約キャンセルポリシー',
                   '前日17時以降のキャンセルはキャンセル料1000円。無断キャンセルは2回で予約制限を検討する。',
                   ARRAY['受付', '運営'])
                ON CONFLICT (id) DO NOTHING
                """);
    }

    private void ensureConstructionDemo() {
        jdbc.update(
                """
                INSERT INTO construction_projects (id, org_id, name, site_address, status, manager_name, start_date, end_date)
                VALUES
                    ('prj-demo-1', 'org_demo', '渋谷オフィスビル新築工事', '東京都渋谷区道1-1-1', 'IN_PROGRESS', '山田 太郎',
                            CURRENT_DATE - 30, CURRENT_DATE + 180),
                    ('prj-demo-2', 'org_demo', '横浜物流センター改修', '神奈川県横浜市西区1-2-3', 'PLANNING', '佐藤 花子',
                            CURRENT_DATE + 14, CURRENT_DATE + 365)
                ON CONFLICT (id) DO NOTHING
                """);
        jdbc.update(
                """
                INSERT INTO project_module_records (id, org_id, project_id, module_code, title, status, detail,
                    amount, person_name, record_date)
                VALUES
                  ('rec-demo-1', 'org_demo', 'prj-demo-1', 'CONSTRUCTION_MGMT', '基礎工程進捗確認', 'IN_PROGRESS',
                   '配筋検査完了、次回コンクリート打設予定', NULL, '山田 太郎', CURRENT_DATE),
                  ('rec-draw-1', 'org_demo', 'prj-demo-1', 'DRAWINGS', '構造図 S-101 rev.3', 'APPROVED',
                   'RC造3階スラブ配筋図・最新版を現場共有', NULL, '佐藤 花子', CURRENT_DATE - 5),
                  ('rec-bb-1', 'org_demo', 'prj-demo-1', 'BLACKBOARD', '3階スラブ打設前確認', 'DONE',
                   '黒板付き写真を現場記録に登録', NULL, '山田 太郎', CURRENT_DATE - 2),
                  ('rec-insp-1', 'org_demo', 'prj-demo-1', 'INSPECTION', '配筋検査 3階スラブ', 'APPROVED',
                   '品質検査合格・写真添付済', NULL, '検査 一郎', CURRENT_DATE - 1),
                  ('rec-board-1', 'org_demo', 'prj-demo-1', 'PROJECT_BOARD', '安全パトロール実施', 'IN_PROGRESS',
                   '来週火曜に全社パトロールを実施', NULL, '安全 太郎', CURRENT_DATE),
                  ('rec-inq-1', 'org_demo', 'prj-demo-1', 'INQUIRY_PROFIT', '本工事確定見積', 'WON',
                   '請負48.5億円・実行予算46.8億円で粗利確保', NULL, '山田 太郎', CURRENT_DATE - 45),
                  ('rec-ord-1', 'org_demo', 'prj-demo-1', 'ORDERS', '鉄骨加工発注', 'ORDERED',
                   '4階H形鋼 450×200 加工発注', 12800000, '調達 次郎', CURRENT_DATE - 10),
                  ('rec-remote-1', 'org_demo', 'prj-demo-1', 'REMOTE_SITE', '遠隔臨場 配筋確認', 'DONE',
                   '360°カメラで遠隔確認完了', NULL, '監理 三郎', CURRENT_DATE - 3),
                  ('rec-doc-1', 'org_demo', 'prj-demo-1', 'DOC_APPROVAL', '安全書類提出', 'IN_PROGRESS',
                   '書類承認ワークフロー申請中', NULL, '山田 太郎', CURRENT_DATE),
                  ('rec-scan-1', 'org_demo', 'prj-demo-1', 'SCAN_3D', 'B1F 3Dスキャン', 'DONE',
                   '点群データ登録済・BIM連携可能', NULL, '測量 四郎', CURRENT_DATE - 7),
                  ('rec-bill-1', 'org_demo', 'prj-demo-1', 'BILLING', '第1回出来高請求', 'PAID',
                   '完了出来高請求・入金確認済', 485000000, '山田 太郎', CURRENT_DATE - 60),
                  ('rec-bill-2', 'org_demo', 'prj-demo-1', 'BILLING', '第2回出来高請求', 'INVOICED',
                   '進捗出来高請求書発行済', 495000000, '山田 太郎', CURRENT_DATE - 30),
                  ('rec-wr-1', 'org_demo', 'prj-demo-1', 'WORK_RATE', '内装工事歩掛', 'OPEN',
                   '㎡あたり0.85人工の標準歩掛', 8500, '積算 五郎', CURRENT_DATE - 14),
                  ('rec-access-1', 'org_demo', 'prj-demo-1', 'SITE_ACCESS', '協力会社入場 15名', 'DONE',
                   'ゲート通過記録・退場確認済', NULL, '警備 六郎', CURRENT_DATE),
                  ('rec-edel-1', 'org_demo', 'prj-demo-1', 'E_DELIVERY', '竣工図書電子納品', 'SUBMITTED',
                   '検査機関への電子納品データ作成', NULL, '技術 七郎', CURRENT_DATE - 20),
                  ('rec-bm-1', 'org_demo', 'prj-demo-1', 'BM', '空調設備定期点検', 'SCHEDULED',
                   '次回点検 8月15日予定', NULL, '設備 八郎', CURRENT_DATE + 30),
                  ('rec-analytics-1', 'org_demo', 'prj-demo-1', 'ANALYTICS', '月次コストレポート', 'DONE',
                   '5月分の原価・進捗分析を確認', NULL, '経営 九郎', CURRENT_DATE - 15),
                  ('rec-draw-2', 'org_demo', 'prj-demo-2', 'DRAWINGS', '改修平面図 A-001', 'OPEN',
                   '横浜物流センター1階改修図面', NULL, '佐藤 花子', CURRENT_DATE - 3),
                  ('rec-ord-2', 'org_demo', 'prj-demo-2', 'ORDERS', '内装資材見積依頼', 'OPEN',
                   '床材・天井材の見積比較', 3200000, '調達 次郎', CURRENT_DATE)
                ON CONFLICT (id) DO NOTHING
                """);
    }

    private void ensureSaasBusinessDemo() {
        jdbc.update(
                """
                INSERT INTO dx_initiatives (id, org_id, title, description, status, progress_pct, owner_name, due_date)
                VALUES
                  ('dx-demo-1', 'org_demo', '現場DX推進', '黒板・図面のデジタル化を全案件に展開', 'IN_PROGRESS', 65, '山田 太郎', CURRENT_DATE + 90),
                  ('dx-demo-2', 'org_demo', 'BIM活用拡大', '施工段階でのBIMモデル活用', 'PLANNED', 20, '佐藤 花子', CURRENT_DATE + 180)
                ON CONFLICT (id) DO NOTHING
                """);
        jdbc.update(
                """
                INSERT INTO dx_tasks (id, org_id, initiative_id, title, done)
                VALUES
                  ('dxt-demo-1', 'org_demo', 'dx-demo-1', '全現場責任者への操作研修', TRUE),
                  ('dxt-demo-2', 'org_demo', 'dx-demo-1', '図面共有ルールの策定', FALSE),
                  ('dxt-demo-3', 'org_demo', 'dx-demo-2', 'BIMビューワー導入検証', FALSE)
                ON CONFLICT (id) DO NOTHING
                """);
        jdbc.update(
                """
                INSERT INTO crm_contacts (id, org_id, name, email, phone, company, stage, notes)
                VALUES
                  ('crm-demo-1', 'org_demo', '株式会社東都開発', 'contact@totodevelop.jp', '03-1234-5678', '東都開発', 'ACTIVE',
                   '渋谷オフィスビル新築の発注者'),
                  ('crm-demo-2', 'org_demo', '横浜ロジスティクス', 'info@yokohama-logi.jp', '045-987-6543', '横浜ロジ', 'LEAD',
                   '物流センター改修の引合')
                ON CONFLICT (id) DO NOTHING
                """);
        jdbc.update(
                """
                INSERT INTO crm_interactions (id, org_id, contact_id, kind, summary, occurred_at)
                VALUES
                  ('crmi-demo-1', 'org_demo', 'crm-demo-1', 'MEETING', '月次定例・工程確認', NOW() - INTERVAL '3 days'),
                  ('crmi-demo-2', 'org_demo', 'crm-demo-2', 'CALL', '改修範囲のヒアリング', NOW() - INTERVAL '1 day')
                ON CONFLICT (id) DO NOTHING
                """);
        jdbc.update(
                """
                INSERT INTO attendance_records (id, org_id, user_id, clock_in, clock_out, note)
                VALUES
                  ('att-demo-1', 'org_demo', 'user_demo', date_trunc('day', NOW()) + INTERVAL '8 hours',
                   date_trunc('day', NOW()) + INTERVAL '17 hours', '本社・渋谷案件打合せ'),
                  ('att-demo-2', 'org_demo', 'user_demo', date_trunc('day', NOW()) - INTERVAL '1 day' + INTERVAL '7 hours 30 minutes',
                   date_trunc('day', NOW()) - INTERVAL '1 day' + INTERVAL '18 hours', '現場立会')
                ON CONFLICT (id) DO NOTHING
                """);
        jdbc.update(
                """
                INSERT INTO leave_requests (id, org_id, user_id, start_date, end_date, reason, status)
                VALUES
                  ('leave-demo-1', 'org_demo', 'user_demo', CURRENT_DATE + 14, CURRENT_DATE + 16, '夏季休暇', 'PENDING')
                ON CONFLICT (id) DO NOTHING
                """);
        jdbc.update(
                """
                INSERT INTO contract_templates (id, org_id, name, body)
                VALUES
                  ('ctpl-demo-1', 'org_demo', '下請基本契約書', '第1条（目的）本契約は…')
                ON CONFLICT (id) DO NOTHING
                """);
        jdbc.update(
                """
                INSERT INTO contracts (id, org_id, template_id, title, party_name, party_email, body, status, signed_at)
                VALUES
                  ('ctr-demo-1', 'org_demo', 'ctpl-demo-1', '躯体工事下請契約', '株式会社××建設', 'contract@xx-kensetsu.jp',
                   '渋谷オフィスビル新築工事 躯体工事', 'SIGNED', NOW() - INTERVAL '30 days'),
                  ('ctr-demo-2', 'org_demo', 'ctpl-demo-1', '設備工事下請契約', '△△電気工業', 'sales@denki.co.jp',
                   '渋谷オフィスビル新築工事 設備工事', 'PENDING', NULL)
                ON CONFLICT (id) DO NOTHING
                """);
        jdbc.update(
                """
                INSERT INTO consultation_threads (id, org_id, user_id, title, created_at)
                VALUES ('consult-demo-1', 'org_demo', 'user_demo', '安全書類の承認フローについて', NOW() - INTERVAL '2 days')
                ON CONFLICT (id) DO NOTHING
                """);
        jdbc.update(
                """
                INSERT INTO consultation_messages (id, org_id, thread_id, role, content, created_at)
                VALUES
                  ('cmsg-demo-1', 'org_demo', 'consult-demo-1', 'user', '資料承認のワークフローはどう進めればよいですか？',
                   NOW() - INTERVAL '2 days'),
                  ('cmsg-demo-2', 'org_demo', 'consult-demo-1', 'assistant',
                   '資料承認モジュールで記録を作成すると、レビュー→最終承認の2段階フローが自動で開始されます。',
                   NOW() - INTERVAL '2 days' + INTERVAL '1 minute')
                ON CONFLICT (id) DO NOTHING
                """);
    }

    private void ensureExtendedDemo() {
        jdbc.update(
                """
                INSERT INTO api_integrations (id, org_id, name, provider, endpoint_url, api_key_hint, status, last_sync_at)
                VALUES
                  ('api-demo-1', 'org_demo', 'kintone 案件連携', 'kintone',
                          'https://example.cybozu.com/k/v1/', '****7a3f', 'ACTIVE', NOW()),
                  ('api-demo-2', 'org_demo', 'freee 会計連携', 'freee',
                          'https://api.freee.co.jp/api/1/', '****9b2c', 'ACTIVE', NOW() - INTERVAL '6 hours'),
                  ('api-demo-3', 'org_demo', 'Box 図面連携', 'box',
                          'https://api.box.com/2.0/', '****1d4e', 'PAUSED', NOW() - INTERVAL '3 days')
                ON CONFLICT (id) DO NOTHING
                """);
        jdbc.update(
                """
                INSERT INTO bim_models (id, org_id, project_id, title, format, viewer_url, file_size_mb, status, uploaded_by)
                VALUES
                  ('bim-demo-1', 'org_demo', 'prj-demo-1', '本館構造BIMモデル v2', 'glTF',
                          'https://modelviewer.dev/shared-assets/models/Astronaut.glb', 128.5, 'READY', '山田 太郎'),
                  ('bim-demo-2', 'org_demo', 'prj-demo-1', '設備BIMモデル v1', 'IFC',
                          'https://modelviewer.dev/shared-assets/models/Astronaut.glb', 86.2, 'READY', '佐藤 花子'),
                  ('bim-demo-3', 'org_demo', 'prj-demo-2', '改修計画BIM', 'glTF',
                          'https://modelviewer.dev/shared-assets/models/Astronaut.glb', 42.0, 'PROCESSING', '佐藤 花子')
                ON CONFLICT (id) DO NOTHING
                """);
    }

    private void ensureBudgetDemo() {
        jdbc.update(
                """
                INSERT INTO project_budgets (id, org_id, project_id, name, budget_type, status, version_no, contract_amount, notes, approved_at)
                VALUES
                  ('bud-demo-1', 'org_demo', 'prj-demo-1', '実行予算 v3', 'EXECUTION_BUDGET', 'APPROVED', 3, 4850000000,
                   '本工事確定後の最終予算', NOW()),
                  ('bud-demo-2', 'org_demo', 'prj-demo-1', '当初見積 v1', 'ESTIMATE', 'LOCKED', 1, 5200000000,
                   '入札時点の見積', NOW())
                ON CONFLICT (id) DO NOTHING
                """);
        jdbc.update(
                """
                INSERT INTO budget_line_items (id, org_id, budget_id, category_code, category_name, wbs_code, description,
                    estimate_amount, budget_amount, committed_amount, actual_amount, sort_order)
                VALUES
                  ('bli-demo-1', 'org_demo', 'bud-demo-1', 'DIRECT', '直接工事費', 'WBS-01', '躯体工事（RC造）',
                   1850000000, 1820000000, 1650000000, 980000000, 1),
                  ('bli-demo-2', 'org_demo', 'bud-demo-1', 'SUBCONTRACT', '外注費', 'WBS-02', '電気・空調設備',
                   980000000, 960000000, 890000000, 520000000, 2),
                  ('bli-demo-3', 'org_demo', 'bud-demo-1', 'MATERIAL', '材料費', 'WBS-03', '鉄骨・コンクリート',
                   720000000, 710000000, 680000000, 410000000, 3)
                ON CONFLICT (id) DO NOTHING
                """);
        jdbc.update(
                """
                INSERT INTO cost_entries (id, org_id, project_id, line_item_id, entry_type, vendor_name, description,
                    amount, entry_date, invoice_no, recorded_by)
                VALUES
                  ('cost-demo-1', 'org_demo', 'prj-demo-1', 'bli-demo-1', 'SUBCONTRACT', '株式会社××建設',
                   '3階スラブコンクリート打設', 28500000, CURRENT_DATE - 3, 'INV-2026-0412', '山田 太郎'),
                  ('cost-demo-2', 'org_demo', 'prj-demo-1', 'bli-demo-3', 'MATERIAL', '日本製鉄株式会社',
                   'H形鋼 4階分納入', 42800000, CURRENT_DATE - 7, 'INV-2026-0398', '佐藤 花子'),
                  ('cost-demo-6', 'org_demo', 'prj-demo-1', 'bli-demo-2', 'SUBCONTRACT', '△△電気工業',
                   '設備工事進捗分', 385000000,
                   date_trunc('month', CURRENT_DATE) - interval '3 months' + interval '20 days',
                   'INV-2026-0208', '山田 太郎')
                ON CONFLICT (id) DO NOTHING
                """);
    }

    private void ensureWorkflowDemo() {
        jdbc.update(
                """
                INSERT INTO wf_definitions (id, org_id, flow_id, name, version, entity_type, description)
                VALUES
                    ('wfdef_generic_single', 'org_demo', 'generic-single-approval', '汎用単段承認', 1, 'GENERIC', '任意業務に使える1段階承認'),
                    ('wfdef_generic_two', 'org_demo', 'generic-two-step-approval', '汎用二段承認', 1, 'GENERIC', '上長→部門長の2段階承認'),
                    ('wfdef_budget', 'org_demo', 'budget-approval', '予算承認', 1, 'PROJECT_BUDGET', '現場責任者→経理の予算承認'),
                    ('wfdef_leave', 'org_demo', 'leave-approval', '休暇申請', 1, 'LEAVE_REQUEST', '休暇申請の上長承認'),
                    ('wfdef_doc', 'org_demo', 'document-approval', '書類承認', 1, 'DOCUMENT', '書類レビュー→最終承認')
                ON CONFLICT (id) DO NOTHING
                """);
        jdbc.update(
                """
                INSERT INTO wf_steps (id, definition_id, step_key, name, step_order, step_type, assignee_type, assignee_value, im_node_id)
                VALUES
                    ('wfstep_gs_submit', 'wfdef_generic_single', 'submit', '起票', 0, 'SUBMIT', 'SUBMITTER', NULL, NULL),
                    ('wfstep_gs_mgr', 'wfdef_generic_single', 'manager_approval', '上長承認', 1, 'APPROVAL', 'ROLE', 'manager', 'node_manager'),
                    ('wfstep_gs_end', 'wfdef_generic_single', 'complete', '完了', 99, 'END', 'ANY', NULL, NULL),
                    ('wfstep_gt_submit', 'wfdef_generic_two', 'submit', '起票', 0, 'SUBMIT', 'SUBMITTER', NULL, NULL),
                    ('wfstep_gt_mgr', 'wfdef_generic_two', 'manager_approval', '上長承認', 1, 'APPROVAL', 'ROLE', 'manager', 'node_manager'),
                    ('wfstep_gt_dir', 'wfdef_generic_two', 'director_approval', '部門長承認', 2, 'APPROVAL', 'ROLE', 'admin', 'node_director'),
                    ('wfstep_gt_end', 'wfdef_generic_two', 'complete', '完了', 99, 'END', 'ANY', NULL, NULL),
                    ('wfstep_bd_submit', 'wfdef_budget', 'submit', '起票', 0, 'SUBMIT', 'SUBMITTER', NULL, NULL),
                    ('wfstep_bd_mgr', 'wfdef_budget', 'manager_approval', '現場責任者承認', 1, 'APPROVAL', 'ROLE', 'manager', 'node_budget_mgr'),
                    ('wfstep_bd_fin', 'wfdef_budget', 'finance_approval', '経理承認', 2, 'APPROVAL', 'ROLE', 'admin', 'node_budget_fin'),
                    ('wfstep_bd_end', 'wfdef_budget', 'complete', '完了', 99, 'END', 'ANY', NULL, NULL),
                    ('wfstep_lv_submit', 'wfdef_leave', 'submit', '起票', 0, 'SUBMIT', 'SUBMITTER', NULL, NULL),
                    ('wfstep_lv_mgr', 'wfdef_leave', 'manager_approval', '上長承認', 1, 'APPROVAL', 'ROLE', 'manager', 'node_leave_mgr'),
                    ('wfstep_lv_end', 'wfdef_leave', 'complete', '完了', 99, 'END', 'ANY', NULL, NULL),
                    ('wfstep_dc_submit', 'wfdef_doc', 'submit', '起票', 0, 'SUBMIT', 'SUBMITTER', NULL, NULL),
                    ('wfstep_dc_rev', 'wfdef_doc', 'reviewer_approval', 'レビュー', 1, 'APPROVAL', 'ROLE', 'manager', 'node_doc_review'),
                    ('wfstep_dc_fin', 'wfdef_doc', 'final_approval', '最終承認', 2, 'APPROVAL', 'ROLE', 'admin', 'node_doc_final'),
                    ('wfstep_dc_end', 'wfdef_doc', 'complete', '完了', 99, 'END', 'ANY', NULL, NULL)
                ON CONFLICT (id) DO NOTHING
                """);
    }

    private void ensureDocApprovalArtifactsDemo() {
        jdbc.update(
                """
                INSERT INTO wf_monitoring_flow_data (
                    id, org_id, flow_id, flow_name,
                    approve_count, approve_end_count, deny_count, discontinue_count, matter_handle_count,
                    minimum_time, maximum_time, average_time, amount_time, count_sum, updated_at
                )
                VALUES (
                    'mon-doc-demo', 'org_demo', 'document-approval', '書類承認',
                    '3', '2', '1', '1', '2',
                    '15', '120', '45', '135', '9', NOW()
                )
                ON CONFLICT (org_id, flow_id) DO UPDATE SET
                    flow_name = EXCLUDED.flow_name,
                    approve_count = EXCLUDED.approve_count,
                    approve_end_count = EXCLUDED.approve_end_count,
                    deny_count = EXCLUDED.deny_count,
                    discontinue_count = EXCLUDED.discontinue_count,
                    matter_handle_count = EXCLUDED.matter_handle_count,
                    minimum_time = EXCLUDED.minimum_time,
                    maximum_time = EXCLUDED.maximum_time,
                    average_time = EXCLUDED.average_time,
                    amount_time = EXCLUDED.amount_time,
                    count_sum = EXCLUDED.count_sum,
                    updated_at = NOW()
                """);
        jdbc.update(
                """
                INSERT INTO mail_messages (
                    id, org_id, mail_id, locale_id, recipients, cc, subject, body,
                    parameters, entity_type, entity_id, flow_id, sent, created_at
                )
                VALUES (
                    'mail-doc-demo-1', 'org_demo', 'andpad-doc-submit', 'ja', 'dev@andpad.local', '',
                    '[ANDPAD] 資料承認申請: 安全書類提出',
                    '山田 太郎 様\n\n資料「安全書類提出」のワークフローが更新されました。\n操作: SUBMIT',
                    '{"title":"安全書類提出","flowId":"document-approval","entityId":"rec-doc-1"}'::jsonb,
                    'DOCUMENT', 'rec-doc-1', 'document-approval', TRUE, NOW() - INTERVAL '1 hour'
                )
                ON CONFLICT (id) DO NOTHING
                """);
    }
}
