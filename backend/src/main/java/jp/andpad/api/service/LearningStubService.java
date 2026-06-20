package jp.andpad.api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import jp.andpad.api.domain.LearningActivityKind;
import jp.andpad.api.domain.LearningTypes.AnalyticsBoard;
import jp.andpad.api.domain.LearningTypes.AnalyticsInsight;
import jp.andpad.api.domain.LearningTypes.Bookmark;
import jp.andpad.api.domain.LearningTypes.Certificate;
import jp.andpad.api.domain.LearningTypes.DashboardStats;
import jp.andpad.api.domain.LearningTypes.Instructor;
import jp.andpad.api.domain.LearningTypes.LearningActivityEvent;
import jp.andpad.api.domain.LearningTypes.LearningPath;
import jp.andpad.api.domain.LearningTypes.Quiz;
import jp.andpad.api.domain.LearningTypes.QuizAttempt;
import jp.andpad.api.domain.LearningTypes.Video;
import jp.andpad.api.domain.LearningTypes.VideoNote;
import jp.andpad.api.domain.LearningTypes.VideoPage;
import jp.andpad.api.domain.LearningTypes.WatchProgress;
import jp.andpad.api.domain.SkillLevel;
import jp.andpad.api.domain.VideoCategory;
import jp.andpad.api.graphql.input.LearningInputs.CreateVideoNoteInput;
import jp.andpad.api.graphql.input.LearningInputs.SubmitQuizAttemptInput;
import jp.andpad.api.graphql.input.LearningInputs.UpdateWatchProgressInput;
import jp.andpad.api.repository.EngagementRepository;
import jp.andpad.api.repository.LearningRepository;
import jp.andpad.api.security.TenantContext;
import jp.andpad.api.util.Dates;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LearningStubService {

    private final LearningRepository learningRepository;
    private final EngagementRepository engagementRepository;

    public DashboardStats dashboard() {
        return learningRepository.dashboard(TenantContext.orgId());
    }

    public VideoPage videos(VideoCategory category, SkillLevel skillLevel, String search, Integer page, Integer pageSize) {
        int p = page == null || page < 1 ? 1 : page;
        int size = pageSize == null || pageSize < 1 ? 20 : pageSize;
        return learningRepository.paginateVideos(TenantContext.orgId(), category, skillLevel, search, p, size);
    }

    public Video video(String id) {
        return learningRepository.getVideo(TenantContext.orgId(), id);
    }

    public List<Video> featuredVideos() {
        return learningRepository.featuredVideos(TenantContext.orgId());
    }

    public List<Instructor> instructors() {
        return learningRepository.listInstructors(TenantContext.orgId());
    }

    public Instructor instructor(String id) {
        return learningRepository.getInstructor(TenantContext.orgId(), id);
    }

    public List<LearningPath> learningPaths(VideoCategory category, SkillLevel skillLevel) {
        return learningRepository.listPaths(TenantContext.orgId(), category, skillLevel);
    }

    public LearningPath learningPath(String id) {
        return learningRepository.getPath(TenantContext.orgId(), id);
    }

    public List<WatchProgress> myProgress(String learnerId) {
        return engagementRepository.listProgress(TenantContext.orgId(), learnerId);
    }

    public List<Bookmark> myBookmarks(String learnerId) {
        return engagementRepository.listBookmarks(TenantContext.orgId(), learnerId);
    }

    public List<VideoNote> videoNotes(String videoId, String learnerId) {
        return engagementRepository.listNotes(TenantContext.orgId(), videoId, learnerId);
    }

    public List<Quiz> quizzes(String videoId) {
        return engagementRepository.listQuizzes(TenantContext.orgId(), videoId);
    }

    public Quiz quiz(String id) {
        return engagementRepository.getQuiz(TenantContext.orgId(), id);
    }

    public List<QuizAttempt> myQuizAttempts(String learnerId) {
        return engagementRepository.listAttempts(TenantContext.orgId(), learnerId);
    }

    public List<Certificate> myCertificates(String learnerId) {
        return engagementRepository.listCertificates(TenantContext.orgId(), learnerId);
    }

    public AnalyticsBoard analyticsBoard(int periodDays) {
        DashboardStats d = dashboard();
        return new AnalyticsBoard(
                periodDays,
                List.of(
                        new jp.andpad.api.domain.LearningTypes.AnalyticsKpi("watch_hours", d.watchHoursThisMonth(), "h", null),
                        new jp.andpad.api.domain.LearningTypes.AnalyticsKpi("completions", d.completionsThisMonth(), "", null),
                        new jp.andpad.api.domain.LearningTypes.AnalyticsKpi("active_learners", d.activeLearners(), "", null),
                        new jp.andpad.api.domain.LearningTypes.AnalyticsKpi("video_library", d.videosTotal(), "", null)),
                List.of(d.watchHoursThisMonth() * 0.2, d.watchHoursThisMonth() * 0.25,
                        d.watchHoursThisMonth() * 0.3, d.watchHoursThisMonth() * 0.25),
                List.of(),
                List.of(),
                d.activeLearners() > 0 ? 72 : 0);
    }

    public AnalyticsInsight analyticsInsight(int periodDays) {
        return new AnalyticsInsight(
                "Learning analytics stub",
                List.of(),
                List.of(),
                List.of("Connect learning data source"),
                Dates.format(Dates.now()));
    }

    public WatchProgress updateWatchProgress(UpdateWatchProgressInput input) {
        WatchProgress progress = new WatchProgress(
                null,
                input.videoId(),
                input.learnerId(),
                input.positionSec(),
                Boolean.TRUE.equals(input.completed()),
                Dates.format(Dates.now()));
        return learningRepository.updateProgress(TenantContext.orgId(), progress);
    }

    public Video recordVideoView(String videoId) {
        Video updated = learningRepository.incrementViewCount(TenantContext.orgId(), videoId);
        return updated != null ? updated : video(videoId);
    }

    public VideoNote createVideoNote(CreateVideoNoteInput input) {
        VideoNote note = new VideoNote(
                null,
                input.videoId(),
                input.learnerId(),
                input.timestampSec(),
                input.body(),
                null);
        return engagementRepository.createNote(TenantContext.orgId(), note);
    }

    public boolean deleteVideoNote(String id) {
        return engagementRepository.deleteNote(TenantContext.orgId(), id);
    }

    public Bookmark toggleBookmark(String videoId, String learnerId) {
        return engagementRepository.toggleBookmark(TenantContext.orgId(), videoId, learnerId);
    }

    public LearningPath enrollLearningPath(String pathId, String learnerId) {
        engagementRepository.enrollPath(TenantContext.orgId(), pathId, learnerId);
        LearningPath path = learningPath(pathId);
        return path != null ? path : learningPaths(null, null).stream()
                .filter(p -> p.id().equals(pathId))
                .findFirst()
                .orElse(new LearningPath(
                        pathId, "", "", VideoCategory.ENDODONTICS, SkillLevel.BEGINNER,
                        List.of(), 0, 0, ""));
    }

    public QuizAttempt submitQuizAttempt(SubmitQuizAttemptInput input) {
        return engagementRepository.submitAttempt(
                TenantContext.orgId(),
                input.quizId(),
                input.learnerId(),
                input.answers());
    }

    public LearningActivityEvent learningActivityEvent(String learnerId) {
        return new LearningActivityEvent(
                LearningActivityKind.PROGRESS_UPDATED,
                learnerId,
                null,
                null,
                null,
                "stub",
                Dates.format(Dates.now()));
    }
}
