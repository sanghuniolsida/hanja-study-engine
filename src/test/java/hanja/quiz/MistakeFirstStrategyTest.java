package hanja.quiz;

import hanja.domain.Hanja;
import hanja.study.LearningRecord;
import hanja.study.UserProgress;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

class MistakeFirstStrategyTest {

    private static final List<Hanja> POOL = List.of(
            new Hanja("一", "일", "하나", "8급"),
            new Hanja("二", "이", "둘",  "8급"),
            new Hanja("三", "삼", "셋",  "8급"),
            new Hanja("人", "인", "사람","8급"),
            new Hanja("力", "력", "힘",  "8급")
    );

    @Test
    void 오답이_있으면_먼저_선별하고_부족분은_랜덤으로_채운다() {
        UserProgress up = new UserProgress();
        up.recordResult(new Hanja("人","인","사람","8급"), false, java.time.LocalDate.now());
        var strat = new MistakeFirstStrategy(up, new Random(42));

        List<Quiz> qs = strat.selectQuizzes(POOL, 3);

        assertThat(qs).hasSize(3);
        Set<String> chars = qs.stream().map(q -> q.hanja().getCharacter()).collect(Collectors.toSet());
        assertThat(chars).contains("人");
        assertThat(qs).extracting(q -> q.hanja().getCharacter()).doesNotHaveDuplicates();
    }

    @Test
    void 오답이_없으면_일반_랜덤처럼_동작한다() {
        UserProgress up = new UserProgress();
        var strat = new MistakeFirstStrategy(up, new Random(7));

        List<Quiz> qs = strat.selectQuizzes(POOL, 4);
        assertThat(qs).hasSize(4);
        var strat2 = new MistakeFirstStrategy(up, new Random(7));
        assertThat(strat2.selectQuizzes(POOL, 4))
                .usingRecursiveComparison().isEqualTo(qs);
    }
}