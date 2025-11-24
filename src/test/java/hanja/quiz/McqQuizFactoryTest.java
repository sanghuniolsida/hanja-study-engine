package hanja.quiz;

import hanja.domain.Hanja;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

class McqQuizFactoryTest {

    @Test
    @DisplayName("TEXT_TO_HANJA: (뜻·음) → 한자 고르기, 보기 4개/정답 포함/중복 없음")
    void textToHanja_basic() {
        List<Hanja> all = fixtures();
        List<Hanja> levelFiltered = all.subList(0, 6);
        Random rng = new Random(42);

        var quizzes = McqQuizFactory.build(
                levelFiltered,
                all,
                5,
                McqQuizFactory.Kind.TEXT_TO_HANJA,
                rng
        );

        assertThat(quizzes).hasSize(5);
        quizzes.forEach(q -> {
            assertThat(q.isMcq()).isTrue();
            assertThat(q.type()).isEqualTo(QuestionType.MCQ_TEXT_TO_HANJA);

            assertThat(q.options()).hasSize(4);
            assertThat(new HashSet<>(q.options())).hasSize(4);

            String correctChar = q.hanja().getCharacter();
            assertThat(q.options()).contains(correctChar);

            assertThat(q.prompt()).contains("뜻:").contains("음:");
        });
    }

    @Test
    @DisplayName("HANJA_TO_TEXT: (한자) → (음 · 뜻) 고르기, 보기 4개/정답 포함/중복 없음")
    void hanjaToText_basic() {
        List<Hanja> all = fixtures();
        List<Hanja> levelFiltered = all;
        Random rng = new Random(7);

        var quizzes = McqQuizFactory.build(
                levelFiltered,
                all,
                4,
                McqQuizFactory.Kind.HANJA_TO_TEXT,
                rng
        );

        assertThat(quizzes).hasSize(4);
        quizzes.forEach(q -> {
            assertThat(q.isMcq()).isTrue();
            assertThat(q.type()).isEqualTo(QuestionType.MCQ_HANJA_TO_TEXT);

            // 보기 4개 & 중복 없음
            assertThat(q.options()).hasSize(4);
            assertThat(new HashSet<>(q.options())).hasSize(4);

            // 정답(“음 · 뜻”)이 반드시 포함
            String correct = q.hanja().getReading() + " · " + q.hanja().getMeaning();
            assertThat(q.options()).contains(correct);

            assertThat(q.prompt()).isEqualTo(q.hanja().getCharacter());
        });
    }

    @Test
    @DisplayName("전체 데이터가 4개 미만이면 MCQ 생성 불가")
    void tooSmallAllData_returnsEmpty() {
        List<Hanja> tiny = List.of(
                new Hanja("一","일","하나","8급"),
                new Hanja("二","이","둘","8급"),
                new Hanja("三","삼","셋","8급")
        );
        Random rng = new Random(1);

        var quizzes = McqQuizFactory.build(
                tiny, tiny, 3, McqQuizFactory.Kind.TEXT_TO_HANJA, rng
        );

        assertThat(quizzes).isEmpty();
    }


    private static List<Hanja> fixtures() {
        return List.of(
                new Hanja("漢","한","한수","7급"),
                new Hanja("海","해","바다","7급"),
                new Hanja("話","화","말씀","7급"),
                new Hanja("活","활","살","7급"),
                new Hanja("人","인","사람","8급"),
                new Hanja("心","심","마음","7급"),
                new Hanja("山","산","메","8급"),
                new Hanja("川","천","내","8급"),
                new Hanja("日","일","날","8급"),
                new Hanja("月","월","달","8급")
        );
    }
}