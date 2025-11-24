package hanja.quiz;

import hanja.domain.Hanja;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class QuizMcqScoringTest {

    @Test
    @DisplayName("MCQ 채점: 인덱스(문자열)로 맞추기 / 보기 텍스트로 맞추기")
    void isCorrect_byIndexOrText() {
        Hanja h = new Hanja("漢", "한", "한수", "7급");
        var options = List.of("海", "話", "漢", "活"); // 정답: "漢"(index 2)
        var q = Quiz.mcq(
                h,
                QuestionType.MCQ_TEXT_TO_HANJA,
                "뜻: 한수 / 음: 한",
                options,
                2
        );

        assertThat(q.isMcq()).isTrue();
        assertThat(q.correctIndex()).isEqualTo(2);
        assertThat(q.options()).containsExactlyElementsOf(options);

        assertThat(q.isCorrect("2")).isTrue();
        assertThat(q.isCorrect("0")).isFalse();

        assertThat(q.isCorrect("漢")).isTrue();
        assertThat(q.isCorrect("海")).isFalse();

        assertThat(q.isCorrect("not-a-number")).isFalse();
        assertThat(q.isCorrect("")).isFalse();
    }

    @Test
    @DisplayName("MCQ 프롬프트 오버라이드가 없을 경우 기존 prompt 규칙을 따른다(회귀 보호)")
    void prompt_fallbackForNonMcqOrNoOverride() {
        Hanja h = new Hanja("海", "해", "바다", "7급");
        var normal = new Quiz(h, QuestionType.HANJA_TO_MEANING);
        assertThat(normal.prompt()).contains("다음 한자의 뜻은").contains("海");
    }
}