package jp.andpad.api.domain;

import java.util.List;

public final class LearningTypes {

    private LearningTypes() {}

    public record DashboardStats(
            int videosTotal,
            int learningPathsTotal,
            int quizzesTotal,
            int completionsThisMonth,
            double watchHoursThisMonth,
            int activeLearners) {}

    public record Instructor(
            String id,
            String name,
            String title,
            String specialty,
            String bio,
            String avatarUrl,
            int videoCount) {}

    public record Video(
            String id,
            String title,
            String description,
            VideoCategory category,
            String procedure,
            SkillLevel skillLevel,
            int durationSec,
            String thumbnailUrl,
            String videoUrl,
            String instructorId,
            String instructorName,
            List<String> tags,
            int viewCount,
            String publishedAt,
            boolean featured) {}

    public record LearningPath(
            String id,
            String title,
            String description,
            VideoCategory category,
            SkillLevel skillLevel,
            List<String> videoIds,
            int estimatedMinutes,
            int enrolledCount,
            String certificateTitle) {}

    public record WatchProgress(
            String id,
            String videoId,
            String learnerId,
            int positionSec,
            boolean completed,
            String updatedAt) {}

    public record VideoNote(
            String id,
            String videoId,
            String learnerId,
            int timestampSec,
            String body,
            String createdAt) {}

    public record QuizChoice(String id, String label) {}

    public record QuizQuestion(String id, String prompt, List<QuizChoice> choices, int correctIndex) {}

    public record Quiz(
            String id,
            String videoId,
            String title,
            int passingScore,
            List<QuizQuestion> questions) {}

    public record QuizAttempt(
            String id,
            String quizId,
            String learnerId,
            int score,
            boolean passed,
            String completedAt) {}

    public record Bookmark(String id, String videoId, String learnerId, String createdAt) {}

    public record Certificate(String id, String pathId, String learnerId, String title, String issuedAt) {}

    public record PageInfo(int total, int page, int pageSize, int totalPages) {}

    public record VideoPage(List<Video> items, PageInfo pageInfo) {}

    public record LearningActivityEvent(
            LearningActivityKind kind,
            String learnerId,
            String videoId,
            String pathId,
            String quizId,
            String message,
            String occurredAt) {}

    public record AnalyticsKpi(String label, double value, String unit, Double trendPct) {}

    public record CategoryMetric(VideoCategory category, int count) {}

    public record VideoMetric(String videoId, String title, int views, int completions) {}

    public record AnalyticsBoard(
            int periodDays,
            List<AnalyticsKpi> kpis,
            List<Double> watchHoursByWeek,
            List<CategoryMetric> completionsByCategory,
            List<VideoMetric> topVideos,
            double learnerEngagementScore) {}

    public record AnalyticsInsight(
            String summary,
            List<String> strengths,
            List<String> risks,
            List<String> recommendations,
            String generatedAt) {}
}
