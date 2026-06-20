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
        ensureExtendedDemo();
        ensureBudgetDemo();
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
                VALUES ('prj-demo-1', 'org_demo', '渋谷オフィスビル新築工事', '東京都渋谷区道1-1-1', 'IN_PROGRESS', '山田 太郎',
                        CURRENT_DATE - 30, CURRENT_DATE + 180)
                ON CONFLICT (id) DO NOTHING
                """);
        jdbc.update(
                """
                INSERT INTO project_module_records (id, org_id, project_id, module_code, title, status, detail,
                    amount, person_name, record_date)
                VALUES
                  ('rec-demo-1', 'org_demo', 'prj-demo-1', 'CONSTRUCTION_MGMT', '基礎工程進捗確認', 'IN_PROGRESS',
                   '配筋筋の配置完了、次回コンクリート打設', NULL, '山田 太郎', CURRENT_DATE),
                  ('rec-bill-1', 'org_demo', 'prj-demo-1', 'BILLING', '第1回部請求', 'PAID',
                   '完了出来高請求・入金済', 485000000, '山田 太郎', CURRENT_DATE - 60),
                  ('rec-bill-2', 'org_demo', 'prj-demo-1', 'BILLING', '第2回部請求', 'INVOICED',
                   '進捗出来高請求発行済', 495000000, '山田 太郎', CURRENT_DATE - 30),
                  ('rec-inq-1', 'org_demo', 'prj-demo-1', 'INQUIRY_PROFIT', '本工事確定見積', 'WON',
                   '請負金額48.5億円に対し実行予算46.8億円の粗利確保', NULL, '山田 太郎', CURRENT_DATE)
                ON CONFLICT (id) DO NOTHING
                """);
    }

    private void ensureExtendedDemo() {
        jdbc.update(
                """
                INSERT INTO api_integrations (id, org_id, name, provider, endpoint_url, api_key_hint, status, last_sync_at)
                VALUES ('api-demo-1', 'org_demo', 'kintone 案件連携', 'kintone',
                        'https://example.cybozu.com/k/v1/', '****7a3f', 'ACTIVE', NOW())
                ON CONFLICT (id) DO NOTHING
                """);
        jdbc.update(
                """
                INSERT INTO bim_models (id, org_id, project_id, title, format, viewer_url, file_size_mb, status, uploaded_by)
                VALUES ('bim-demo-1', 'org_demo', 'prj-demo-1', '本館構造BIMモデル v2', 'glTF',
                        'https://modelviewer.dev/shared-assets/models/Astronaut.glb', 128.5, 'READY', '山田 太郎')
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
}
