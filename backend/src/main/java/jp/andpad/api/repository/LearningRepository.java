package jp.andpad.api.repository;

import java.sql.Array;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jp.andpad.api.domain.LearningTypes.DashboardStats;
import jp.andpad.api.domain.LearningTypes.Instructor;
import jp.andpad.api.domain.LearningTypes.LearningPath;
import jp.andpad.api.domain.LearningTypes.PageInfo;
import jp.andpad.api.domain.LearningTypes.Video;
import jp.andpad.api.domain.LearningTypes.VideoPage;
import jp.andpad.api.domain.LearningTypes.WatchProgress;
import jp.andpad.api.domain.SkillLevel;
import jp.andpad.api.domain.VideoCategory;
import jp.andpad.api.util.Dates;
import jp.andpad.api.util.Ids;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class LearningRepository {

    private final JdbcTemplate jdbc;

    public DashboardStats dashboard(String orgId) {
        int videosTotal = queryInt("SELECT COUNT(*) FROM videos WHERE org_id = ?", orgId);
        int pathsTotal = queryInt("SELECT COUNT(*) FROM learning_paths WHERE org_id = ?", orgId);
        int quizzesTotal = queryInt("SELECT COUNT(*) FROM quizzes WHERE org_id = ?", orgId);
        Integer[] progress = jdbc.queryForObject(
                """
                SELECT COUNT(*) FILTER (WHERE completed), COALESCE(SUM(position_sec), 0)
                FROM watch_progress WHERE org_id = ?
                """,
                (rs, rowNum) -> new Integer[] { rs.getInt(1), rs.getInt(2) },
                orgId);
        int completions = progress == null ? 0 : progress[0];
        double watchHours = progress == null ? 0 : progress[1] / 3600.0;
        int activeLearners = queryInt(
                "SELECT COUNT(DISTINCT learner_id) FROM watch_progress WHERE org_id = ?",
                orgId);
        return new DashboardStats(
                videosTotal, pathsTotal, quizzesTotal, completions, watchHours, activeLearners);
    }

    public VideoPage paginateVideos(
            String orgId, VideoCategory category, SkillLevel skillLevel, String search, int page, int pageSize) {
        int p = page < 1 ? 1 : page;
        int size = pageSize < 1 ? 12 : pageSize;
        StringBuilder where = new StringBuilder("WHERE v.org_id = ?");
        List<Object> args = new ArrayList<>();
        args.add(orgId);
        if (category != null) {
            where.append(" AND v.category = ?");
            args.add(category.name());
        }
        if (skillLevel != null) {
            where.append(" AND v.skill_level = ?");
            args.add(skillLevel.name());
        }
        if (search != null && !search.isBlank()) {
            where.append(" AND (LOWER(v.title) LIKE ? OR LOWER(v.description) LIKE ?)");
            String pattern = "%" + search.trim().toLowerCase() + "%";
            args.add(pattern);
            args.add(pattern);
        }
        int total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM videos v " + where,
                Integer.class,
                args.toArray());
        int totalPages = Math.max(1, (int) Math.ceil((double) total / size));
        int offset = (p - 1) * size;
        args.add(size);
        args.add(offset);
        List<Video> items = jdbc.query(
                """
                SELECT v.id, v.title, v.description, v.category, v.procedure, v.skill_level, v.duration_sec,
                       v.thumbnail_url, v.video_url, v.instructor_id, COALESCE(i.name, '') AS instructor_name, v.tags,
                       v.view_count, v.featured, v.published_at
                FROM videos v
                LEFT JOIN instructors i ON i.id = v.instructor_id AND i.org_id = v.org_id
                """
                        + where
                        + " ORDER BY v.published_at DESC LIMIT ? OFFSET ?",
                (rs, rowNum) -> mapVideo(rs),
                args.toArray());
        return new VideoPage(items, new PageInfo(total, p, size, totalPages));
    }

    public Video getVideo(String orgId, String id) {
        try {
            return jdbc.queryForObject(
                    """
                    SELECT v.id, v.title, v.description, v.category, v.procedure, v.skill_level, v.duration_sec,
                           v.thumbnail_url, v.video_url, v.instructor_id, COALESCE(i.name, '') AS instructor_name, v.tags,
                           v.view_count, v.featured, v.published_at
                    FROM videos v
                    LEFT JOIN instructors i ON i.id = v.instructor_id AND i.org_id = v.org_id
                    WHERE v.org_id = ? AND v.id = ?
                    """,
                    (rs, rowNum) -> mapVideo(rs),
                    orgId,
                    id);
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    public List<Video> featuredVideos(String orgId) {
        return jdbc.query(
                """
                SELECT v.id, v.title, v.description, v.category, v.procedure, v.skill_level, v.duration_sec,
                       v.thumbnail_url, v.video_url, v.instructor_id, COALESCE(i.name, '') AS instructor_name, v.tags,
                       v.view_count, v.featured, v.published_at
                FROM videos v
                LEFT JOIN instructors i ON i.id = v.instructor_id AND i.org_id = v.org_id
                WHERE v.org_id = ? AND v.featured = TRUE
                ORDER BY v.view_count DESC LIMIT 12
                """,
                (rs, rowNum) -> mapVideo(rs),
                orgId);
    }

    @Transactional
    public Video incrementViewCount(String orgId, String id) {
        int updated = jdbc.update(
                "UPDATE videos SET view_count = view_count + 1 WHERE org_id = ? AND id = ?",
                orgId,
                id);
        if (updated == 0) {
            return null;
        }
        return getVideo(orgId, id);
    }

    public List<Instructor> listInstructors(String orgId) {
        return jdbc.query(
                """
                SELECT i.id, i.name, i.title, i.specialty, i.bio, i.avatar_url,
                       (SELECT COUNT(*) FROM videos v WHERE v.instructor_id = i.id AND v.org_id = ?)::int AS video_count
                FROM instructors i WHERE i.org_id = ? ORDER BY i.name
                """,
                (rs, rowNum) -> new Instructor(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("title"),
                        rs.getString("specialty"),
                        rs.getString("bio"),
                        rs.getString("avatar_url"),
                        rs.getInt("video_count")),
                orgId,
                orgId);
    }

    public Instructor getInstructor(String orgId, String id) {
        try {
            return jdbc.queryForObject(
                    """
                    SELECT i.id, i.name, i.title, i.specialty, i.bio, i.avatar_url,
                           (SELECT COUNT(*) FROM videos v WHERE v.instructor_id = i.id AND v.org_id = ?)::int AS video_count
                    FROM instructors i WHERE i.org_id = ? AND i.id = ?
                    """,
                    (rs, rowNum) -> new Instructor(
                            rs.getString("id"),
                            rs.getString("name"),
                            rs.getString("title"),
                            rs.getString("specialty"),
                            rs.getString("bio"),
                            rs.getString("avatar_url"),
                            rs.getInt("video_count")),
                    orgId,
                    orgId,
                    id);
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    public List<LearningPath> listPaths(String orgId, VideoCategory category, SkillLevel skillLevel) {
        StringBuilder sql = new StringBuilder(
                """
                SELECT id, title, description, category, skill_level, estimated_minutes, enrolled_count, certificate_title
                FROM learning_paths WHERE org_id = ?
                """);
        List<Object> args = new ArrayList<>();
        args.add(orgId);
        if (category != null) {
            sql.append(" AND category = ?");
            args.add(category.name());
        }
        if (skillLevel != null) {
            sql.append(" AND skill_level = ?");
            args.add(skillLevel.name());
        }
        return jdbc.query(
                sql.toString(),
                (rs, rowNum) -> mapPath(rs, pathVideoIds(rs.getString("id"))),
                args.toArray());
    }

    public LearningPath getPath(String orgId, String id) {
        try {
            return jdbc.queryForObject(
                    """
                    SELECT id, title, description, category, skill_level, estimated_minutes, enrolled_count, certificate_title
                    FROM learning_paths WHERE org_id = ? AND id = ?
                    """,
                    (rs, rowNum) -> mapPath(rs, pathVideoIds(id)),
                    orgId,
                    id);
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    @Transactional
    public WatchProgress updateProgress(String orgId, WatchProgress progress) {
        String id = progress.id() == null || progress.id().isBlank() ? Ids.random("wp_") : progress.id();
        Timestamp now = Timestamp.from(Dates.now());
        jdbc.update(
                """
                INSERT INTO watch_progress (id, org_id, video_id, learner_id, position_sec, completed, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (org_id, video_id, learner_id) DO UPDATE SET
                    position_sec = EXCLUDED.position_sec,
                    completed = EXCLUDED.completed,
                    updated_at = EXCLUDED.updated_at
                """,
                id,
                orgId,
                progress.videoId(),
                progress.learnerId(),
                progress.positionSec(),
                progress.completed(),
                now);
        return new WatchProgress(
                id,
                progress.videoId(),
                progress.learnerId(),
                progress.positionSec(),
                progress.completed(),
                Dates.format(now.toInstant()));
    }

    private List<String> pathVideoIds(String pathId) {
        return jdbc.query(
                "SELECT video_id FROM path_videos WHERE path_id = ? ORDER BY sort_order",
                (rs, rowNum) -> rs.getString("video_id"),
                pathId);
    }

    private LearningPath mapPath(java.sql.ResultSet rs, List<String> videoIds) throws java.sql.SQLException {
        return new LearningPath(
                rs.getString("id"),
                rs.getString("title"),
                rs.getString("description"),
                VideoCategory.valueOf(rs.getString("category")),
                SkillLevel.valueOf(rs.getString("skill_level")),
                videoIds,
                rs.getInt("estimated_minutes"),
                rs.getInt("enrolled_count"),
                rs.getString("certificate_title"));
    }

    private Video mapVideo(java.sql.ResultSet rs) throws java.sql.SQLException {
        Timestamp published = rs.getTimestamp("published_at");
        return new Video(
                rs.getString("id"),
                rs.getString("title"),
                rs.getString("description"),
                VideoCategory.valueOf(rs.getString("category")),
                rs.getString("procedure"),
                SkillLevel.valueOf(rs.getString("skill_level")),
                rs.getInt("duration_sec"),
                rs.getString("thumbnail_url"),
                rs.getString("video_url"),
                rs.getString("instructor_id"),
                rs.getString("instructor_name"),
                readTags(rs),
                rs.getInt("view_count"),
                published == null ? Dates.format(Dates.now()) : Dates.format(published.toInstant()),
                rs.getBoolean("featured"));
    }

    private List<String> readTags(java.sql.ResultSet rs) throws java.sql.SQLException {
        Array arr = rs.getArray("tags");
        if (arr == null) {
            return List.of();
        }
        Object raw = arr.getArray();
        if (raw instanceof String[] strings) {
            return Arrays.asList(strings);
        }
        return List.of();
    }

    private int queryInt(String sql, Object... args) {
        Integer value = jdbc.queryForObject(sql, Integer.class, args);
        return value == null ? 0 : value;
    }
}
