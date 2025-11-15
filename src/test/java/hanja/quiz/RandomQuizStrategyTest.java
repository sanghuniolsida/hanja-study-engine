package hanja.quiz;

import hanja.domain.Hanja;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class RandomQuizStrategyTest {

    private static final List<Hanja> POOL = List.of(
            new Hanja("一", "일", "하나", "8급"),
            new Hanja("二", "이", "둘",  "8급"),
            new Hanja("三", "삼", "셋",  "8급"),
            new Hanja("人", "인", "사람","8급"),
            new Hanja("力", "력", "힘",  "8급")
    );

    @Test
    void 요청_개수만큼_풀에서_무작위_출제() {
        var strat = new RandomQuizStrategy(new Random(42L));
        List<Quiz> quizzes = strat.selectQuizzes(POOL, 3);

        assertThat(quizzes).hasSize(3);

        // 풀에 없는 한자가 나오면 안 됨
        Set<String> poolChars = POOL.stream().map(Hanja::getCharacter).collect(java.util.stream.Collectors.toSet());
        assertThat(quizzes.stream().map(q -> q.hanja().getCharacter())).allMatch(poolChars::contains);

        var strat2 = new RandomQuizStrategy(new Random(42L));
        List<Quiz> again = strat2.selectQuizzes(POOL, 3);
        assertThat(again).usingRecursiveComparison().isEqualTo(quizzes);
    }

    @Test
    void 요청_개수가_풀보다_크면_풀_크기까지만() {
        var strat = new RandomQuizStrategy(new Random(1));
        List<Quiz> quizzes = strat.selectQuizzes(POOL, 99);
        assertThat(quizzes).hasSize(POOL.size());
    }
}