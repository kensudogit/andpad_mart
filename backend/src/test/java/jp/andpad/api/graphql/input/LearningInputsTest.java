package jp.andpad.api.graphql.input;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jp.andpad.api.graphql.input.LearningInputs;
import java.util.List;

class LearningInputsTest {

    @Test
    void nestedInputsConstruct() {
        var input = new LearningInputs.UpdateWatchProgressInput("v-1", "user_demo", 30, false);
        assertThat(input.videoId()).isEqualTo("v-1");
    }

}
