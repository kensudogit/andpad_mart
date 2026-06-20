package jp.andpad.api.graphql.input;

import java.util.List;

public final class LearningInputs {

    private LearningInputs() {}

    public record UpdateWatchProgressInput(String videoId, String learnerId, int positionSec, Boolean completed) {}

    public record CreateVideoNoteInput(String videoId, String learnerId, int timestampSec, String body) {}

    public record SubmitQuizAttemptInput(String quizId, String learnerId, List<Integer> answers) {}

    public record CreateRagDocumentInput(String title, String content, List<String> tags) {}

    public record CreateApiIntegrationInput(
            String name, String provider, String endpointUrl, String apiKeyHint) {}

    public record CreateBimModelInput(
            String projectId, String title, String format, String viewerUrl, Double fileSizeMb, String uploadedBy) {}
}
