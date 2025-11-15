package hanja.quiz;

import hanja.domain.Hanja;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class QuizTest {

    private static final Hanja H = new Hanja("人", "인", "사람", "8급");

    @Test
    void 한자_뜻_문제_채점() {
        Quiz q = new Quiz(H, QuestionType.HANJA_TO_MEANING);
        assertThat(q.prompt()).contains("人");
        assertThat(q.isCorrect("사람")).isTrue();
        assertThat(q.isCorrect(" 사람 ")).isTrue();
        assertThat(q.isCorrect("인")).isFalse();
    }

    @Test
    void 뜻_한자_문제_채점() {
        Quiz q = new Quiz(H, QuestionType.MEANING_TO_HANJA);
        assertThat(q.prompt()).contains("뜻");
        assertThat(q.isCorrect("人")).isTrue();
        assertThat(q.isCorrect("人 ")).isTrue();
        assertThat(q.isCorrect("인")).isFalse();
    }

    @Test
    void 한자_음_문제_채점() {
        Quiz q = new Quiz(H, QuestionType.HANJA_TO_READING);
        assertThat(q.prompt()).contains("人");
        assertThat(q.isCorrect("인")).isTrue();
        assertThat(q.isCorrect(" 인 ")).isTrue();
        assertThat(q.isCorrect("사람")).isFalse();
    }
}
