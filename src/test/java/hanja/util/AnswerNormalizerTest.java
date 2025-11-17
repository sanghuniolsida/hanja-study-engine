package hanja.util;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class AnswerNormalizerTest {

    @Test
    void 공백과_대소문자() {
        assertThat(AnswerNormalizer.normalize("  사람  ")).isEqualTo("사람");
        assertThat(AnswerNormalizer.normalize("HELLO   world")).isEqualTo("hello world");
    }

    @Test
    void 전각반각_괄호_따옴표() {
        assertThat(AnswerNormalizer.normalize("（人）")).isEqualTo("人"); // 전각 괄호 + NFKC
        assertThat(AnswerNormalizer.normalize("\"사람\"")).isEqualTo("사람");
        assertThat(AnswerNormalizer.normalize("'인'")).isEqualTo("인");
    }
}