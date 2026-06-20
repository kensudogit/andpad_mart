package jp.andpad.api.demo;

import java.util.List;

/** Go 版 demo/catalog.go と同等の org_demo カタログ定義。 */
public final class DemoCatalog {

    private DemoCatalog() {}

    public record CatalogInstructor(String id, String name, String title, String specialty, String bio) {}

    public record CatalogVideo(
            String id,
            String title,
            String description,
            String category,
            String procedure,
            String skillLevel,
            int durationSec,
            String youtubeId,
            String instructorId,
            boolean featured) {

        public String thumbnailUrl() {
            return "https://img.youtube.com/vi/" + youtubeId + "/hqdefault.jpg";
        }

        public String embedUrl() {
            return "https://www.youtube.com/embed/" + youtubeId;
        }
    }

    public record CatalogPath(
            String id,
            String title,
            String description,
            String category,
            String skillLevel,
            List<String> videoIds,
            int estimatedMinutes,
            int enrolledCount,
            String certificate) {}

    public static List<CatalogInstructor> instructors() {
        return List.of(
                new CatalogInstructor("inst-1", "田中 健一", "歯科医師", "歯内療法", "大学病院歯内科"),
                new CatalogInstructor("inst-2", "佐藤 美咲", "歯科衛生士", "歯周治療", "SRP指導"),
                new CatalogInstructor("inst-3", "鈴木 大輔", "歯科医師", "口腔外科", "インプラント"));
    }

    public static List<CatalogVideo> videos() {
        return List.of(
                new CatalogVideo("v-1", "根管治療 Step1 - 開窩とアクセス",
                        "適切なアクセス窩形成と根管入口の確認手技を解説します。", "ENDODONTICS", "根管治療",
                        "BEGINNER", 720, "JLeiDmOVfcg", "inst-1", true),
                new CatalogVideo("v-2", "ワーク長測定と電気長測定器の使い方",
                        "APEXロケータの読み取りと臨床判断のポイント。", "ENDODONTICS", "根管治療",
                        "INTERMEDIATE", 540, "qCBDpi7cQz4", "inst-1", true),
                new CatalogVideo("v-3", "SRP 基本手技 - スケーラーの角度とストローク",
                        "歯石除去の基本動作と患者への説明のコツ。", "PERIODONTICS", "SRP",
                        "BEGINNER", 600, "LSJto5PVCoY", "inst-2", true),
                new CatalogVideo("v-4", "インプラント埋入 - フラップデザインと治療の流れ",
                        "インプラント治療の全体像と埋入部位の準備手順を解説します。", "IMPLANT", "インプラント",
                        "ADVANCED", 900, "g-i3P-D6p7M", "inst-3", true),
                new CatalogVideo("v-5", "単独インプラント - 治療計画と埋入の要点",
                        "単独歯欠損へのインプラント治療の流れと審美・機能の考え方。", "IMPLANT", "インプラント",
                        "INTERMEDIATE", 660, "8gVfdyASewA", "inst-3", false),
                new CatalogVideo("v-6", "感染対策 - 滅菌サイクルとトレーサビリティ",
                        "クラスBオートクレーブ運用の標準手順。", "INFECTION_CONTROL", "滅菌",
                        "BEGINNER", 480, "CO-CTNmpLc8", "inst-2", true),
                new CatalogVideo("v-7", "小児歯科 - 初診の流れと行動管理の基本",
                        "低侵襲で診療を進めるための声かけ・ポジショニング・保護者説明の要点。", "PEDIATRIC", "行動管理",
                        "BEGINNER", 420, "GGJRR5RsalU", "inst-2", true),
                new CatalogVideo("v-8", "抜歯 - 難抜歯の分割とエレベーション",
                        "ルート破折リスクを下げる分割抜歯の手順。", "ORAL_SURGERY", "抜歯",
                        "ADVANCED", 840, "oVSss3AgCt4", "inst-3", false),
                new CatalogVideo("v-9", "矯正治療の基礎 - ブラケットの仕組みと治療の流れ",
                        "矯正装置の働きと治療経過の説明ポイントを学びます。", "ORTHODONTICS", "矯正",
                        "BEGINNER", 540, "eTSZGIic8cE", "inst-1", true),
                new CatalogVideo("v-10", "口腔放射線画像 - 読影の基本",
                        "パノラマ・デンタルフィルムの見方と異常所見の読み取り方。", "RADIOLOGY", "画像診断",
                        "BEGINNER", 600, "Xfx8D4v5L70", "inst-1", true));
    }

    public static List<CatalogPath> paths() {
        return List.of(
                new CatalogPath("path-1", "根管治療 基礎コース",
                        "開窩から長測定まで、初めて根管治療に取り組む方のためのカリキュラム。",
                        "ENDODONTICS", "BEGINNER", List.of("v-1", "v-2"), 25, 128, "根管治療 基礎修了"),
                new CatalogPath("path-2", "歯周治療スターター",
                        "SRPと患者指導の実践スキルを段階的に習得。", "PERIODONTICS", "BEGINNER",
                        List.of("v-3", "v-6"), 18, 256, "歯周治療スターター修了"),
                new CatalogPath("path-3", "インプラント外科 入門",
                        "埋入の基礎から外科的アプローチまで。", "IMPLANT", "ADVANCED",
                        List.of("v-4", "v-5"), 25, 64, "インプラント外科 入門修了"),
                new CatalogPath("path-4", "矯正治療 入門",
                        "矯正治療の流れと患者説明の基礎。", "ORTHODONTICS", "BEGINNER",
                        List.of("v-9"), 10, 48, "矯正治療 入門修了"),
                new CatalogPath("path-5", "小児歯科 入門",
                        "初診対応と行動管理の実践スキル。", "PEDIATRIC", "BEGINNER",
                        List.of("v-7"), 8, 72, "小児歯科 入門修了"),
                new CatalogPath("path-6", "画像診断 入門",
                        "口腔放射線画像の読影の基本。", "RADIOLOGY", "BEGINNER",
                        List.of("v-10"), 10, 56, "画像診断 入門修了"));
    }
}
