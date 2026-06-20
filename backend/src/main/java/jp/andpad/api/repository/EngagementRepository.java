package jp.andpad.api.repository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jp.andpad.api.domain.LearningTypes.Bookmark;
import jp.andpad.api.domain.LearningTypes.Certificate;
import jp.andpad.api.domain.LearningTypes.Quiz;
import jp.andpad.api.domain.LearningTypes.QuizAttempt;
import jp.andpad.api.domain.LearningTypes.QuizChoice;
import jp.andpad.api.domain.LearningTypes.QuizQuestion;
import jp.andpad.api.domain.LearningTypes.VideoNote;
import jp.andpad.api.domain.LearningTypes.WatchProgress;
import jp.andpad.api.util.Dates;
import jp.andpad.api.util.Ids;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class EngagementRepository {

    private final JdbcTemplate jdbc;

    public List<WatchProgress> listProgress(String orgId, String learnerId) {
        return jdbc.query(
                """
                SELECT id, video_id, learner_id, position_sec, completed, updated_at
                FROM watch_progress WHERE org_id = ? AND learner_id = ?
                ORDER BY updated_at DESC
                """,
                (rs, rowNum) -> new WatchProgress(
                        rs.getString("id"),
                        rs.getString("video_id"),
                        rs.getString("learner_id"),
                        rs.getInt("position_sec"),
                        rs.getBoolean("completed"),
                        formatTimestamp(rs.getTimestamp("updated_at"))),
                orgId,
                learnerId);
    }

    public List<VideoNote> listNotes(String orgId, String videoId, String learnerId) {
        return jdbc.query(
                """
                SELECT id, video_id, learner_id, timestamp_sec, body, created_at
                FROM video_notes
                WHERE org_id = ? AND video_id = ? AND learner_id = ?
                ORDER BY timestamp_sec
                """,
                (rs, rowNum) -> new VideoNote(
                        rs.getString("id"),
                        rs.getString("video_id"),
                        rs.getString("learner_id"),
                        rs.getInt("timestamp_sec"),
                        rs.getString("body"),
                        formatTimestamp(rs.getTimestamp("created_at"))),
                orgId,
                videoId,
                learnerId);
    }

    @Transactional
    public VideoNote createNote(String orgId, VideoNote note) {
        String id = note.id() == null || note.id().isBlank() ? Ids.random("note_") : note.id();
        Timestamp now = Timestamp.from(Dates.now());
        jdbc.update(
                """
                INSERT INTO video_notes (id, org_id, video_id, learner_id, timestamp_sec, body, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                id,
                orgId,
                note.videoId(),
                note.learnerId(),
                note.timestampSec(),
                note.body(),
                now);
        return new VideoNote(
                id, note.videoId(), note.learnerId(), note.timestampSec(), note.body(),
                Dates.format(now.toInstant()));
    }

    @Transactional
    public boolean deleteNote(String orgId, String id) {
        return jdbc.update("DELETE FROM video_notes WHERE org_id = ? AND id = ?", orgId, id) > 0;
    }

    public List<Bookmark> listBookmarks(String orgId, String learnerId) {
        return jdbc.query(
                """
                SELECT id, video_id, learner_id, created_at FROM bookmarks
                WHERE org_id = ? AND learner_id = ? ORDER BY created_at DESC
                """,
                (rs, rowNum) -> new Bookmark(
                        rs.getString("id"),
                        rs.getString("video_id"),
                        rs.getString("learner_id"),
                        formatTimestamp(rs.getTimestamp("created_at"))),
                orgId,
                learnerId);
    }

    @Transactional
    public Bookmark toggleBookmark(String orgId, String videoId, String learnerId) {
        try {
            Bookmark existing = jdbc.queryForObject(
                    """
                    SELECT id, video_id, learner_id, created_at FROM bookmarks
                    WHERE org_id = ? AND video_id = ? AND learner_id = ?
                    """,
                    (rs, rowNum) -> new Bookmark(
                            rs.getString("id"),
                            rs.getString("video_id"),
                            rs.getString("learner_id"),
                            formatTimestamp(rs.getTimestamp("created_at"))),
                    orgId,
                    videoId,
                    learnerId);
            jdbc.update("DELETE FROM bookmarks WHERE id = ?", existing.id());
            return null;
        } catch (EmptyResultDataAccessException ex) {
            String id = Ids.random("bm_");
            Timestamp now = Timestamp.from(Dates.now());
            jdbc.update(
                    """
                    INSERT INTO bookmarks (id, org_id, video_id, learner_id, created_at)
                    VALUES (?, ?, ?, ?, ?)
                    """,
                    id,
                    orgId,
                    videoId,
                    learnerId,
                    now);
            return new Bookmark(id, videoId, learnerId, Dates.format(now.toInstant()));
        }
    }

    public List<Quiz> listQuizzes(String orgId, String videoId) {
        StringBuilder sql = new StringBuilder(
                """
                SELECT id, COALESCE(video_id, '') AS video_id, title, passing_score FROM quizzes WHERE org_id = ?
                """);
        List<Object> args = new ArrayList<>();
        args.add(orgId);
        if (videoId != null && !videoId.isBlank()) {
            sql.append(" AND video_id = ?");
            args.add(videoId);
        }
        List<Quiz> quizzes = jdbc.query(
                sql.toString(),
                (rs, rowNum) -> new Quiz(
                        rs.getString("id"),
                        rs.getString("video_id"),
                        rs.getString("title"),
                        rs.getInt("passing_score"),
                        List.of()),
                args.toArray());
        List<Quiz> out = new ArrayList<>();
        for (Quiz quiz : quizzes) {
            out.add(new Quiz(
                    quiz.id(), quiz.videoId(), quiz.title(), quiz.passingScore(), loadQuestions(quiz.id())));
        }
        return out;
    }

    public Quiz getQuiz(String orgId, String id) {
        try {
            Quiz quiz = jdbc.queryForObject(
                    """
                    SELECT id, COALESCE(video_id, '') AS video_id, title, passing_score FROM quizzes
                    WHERE org_id = ? AND id = ?
                    """,
                    (rs, rowNum) -> new Quiz(
                            rs.getString("id"),
                            rs.getString("video_id"),
                            rs.getString("title"),
                            rs.getInt("passing_score"),
                            List.of()),
                    orgId,
                    id);
            return new Quiz(
                    quiz.id(), quiz.videoId(), quiz.title(), quiz.passingScore(), loadQuestions(quiz.id()));
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    public List<QuizAttempt> listAttempts(String orgId, String learnerId) {
        return jdbc.query(
                """
                SELECT id, quiz_id, learner_id, score, passed, completed_at FROM quiz_attempts
                WHERE org_id = ? AND learner_id = ? ORDER BY completed_at DESC
                """,
                (rs, rowNum) -> new QuizAttempt(
                        rs.getString("id"),
                        rs.getString("quiz_id"),
                        rs.getString("learner_id"),
                        rs.getInt("score"),
                        rs.getBoolean("passed"),
                        formatTimestamp(rs.getTimestamp("completed_at"))),
                orgId,
                learnerId);
    }

    @Transactional
    public QuizAttempt submitAttempt(String orgId, String quizId, String learnerId, List<Integer> answers) {
        Quiz quiz = getQuiz(orgId, quizId);
        if (quiz == null) {
            throw new IllegalArgumentException("quiz not found: " + quizId);
        }
        int correct = 0;
        List<QuizQuestion> questions = quiz.questions();
        for (int i = 0; i < questions.size(); i++) {
            if (answers != null && i < answers.size() && answers.get(i) == questions.get(i).correctIndex()) {
                correct++;
            }
        }
        int score = questions.isEmpty() ? 0 : (int) ((double) correct / questions.size() * 100);
        boolean passed = score >= quiz.passingScore();
        String id = Ids.random("qa_");
        Timestamp now = Timestamp.from(Dates.now());
        jdbc.update(
                """
                INSERT INTO quiz_attempts (id, org_id, quiz_id, learner_id, score, passed, completed_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                id,
                orgId,
                quizId,
                learnerId,
                score,
                passed,
                now);
        return new QuizAttempt(
                id, quizId, learnerId, score, passed, Dates.format(now.toInstant()));
    }

    public List<Certificate> listCertificates(String orgId, String learnerId) {
        return jdbc.query(
                """
                SELECT id, path_id, learner_id, title, issued_at FROM certificates
                WHERE org_id = ? AND learner_id = ? ORDER BY issued_at DESC
                """,
                (rs, rowNum) -> new Certificate(
                        rs.getString("id"),
                        rs.getString("path_id"),
                        rs.getString("learner_id"),
                        rs.getString("title"),
                        formatTimestamp(rs.getTimestamp("issued_at"))),
                orgId,
                learnerId);
    }

    @Transactional
    public void enrollPath(String orgId, String pathId, String learnerId) {
        jdbc.update(
                """
                INSERT INTO enrollments (org_id, path_id, learner_id) VALUES (?, ?, ?)
                ON CONFLICT DO NOTHING
                """,
                orgId,
                pathId,
                learnerId);
        jdbc.update(
                """
                UPDATE learning_paths SET enrolled_count = enrolled_count + 1
                WHERE id = ? AND org_id = ?
                """,
                pathId,
                orgId);
    }

    private List<QuizQuestion> loadQuestions(String quizId) {
        List<QuizQuestion> questions = jdbc.query(
                """
                SELECT id, prompt, correct_index FROM quiz_questions
                WHERE quiz_id = ? ORDER BY sort_order
                """,
                (rs, rowNum) -> new QuizQuestion(
                        rs.getString("id"),
                        rs.getString("prompt"),
                        List.of(),
                        rs.getInt("correct_index")),
                quizId);
        for (int i = 0; i < questions.size(); i++) {
            QuizQuestion q = questions.get(i);
            List<QuizChoice> choices = jdbc.query(
                    """
                    SELECT id, label FROM quiz_choices WHERE question_id = ? ORDER BY sort_order
                    """,
                    (rs, rowNum) -> new QuizChoice(rs.getString("id"), rs.getString("label")),
                    q.id());
            questions.set(i, new QuizQuestion(q.id(), q.prompt(), choices, q.correctIndex()));
        }
        return questions;
    }

    private static String formatTimestamp(Timestamp ts) {
        return ts == null ? Dates.format(Dates.now()) : Dates.format(ts.toInstant());
    }
}
