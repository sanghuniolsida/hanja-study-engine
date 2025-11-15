package hanja.quiz;

import hanja.domain.Hanja;
import java.util.*;

public class RandomQuizStrategy implements QuizStrategy {

    private final Random random;

    public RandomQuizStrategy() {
        this(new Random());
    }

    public RandomQuizStrategy(Random random) {
        this.random = Objects.requireNonNull(random);
    }

    @Override
    public List<Quiz> selectQuizzes(List<Hanja> pool, int count) {
        if (pool == null || pool.isEmpty() || count <= 0) return List.of();

        List<Hanja> copy = new ArrayList<>(pool);
        Collections.shuffle(copy, random);

        int n = Math.min(count, copy.size());

        QuestionType[] types = QuestionType.values();
        List<Quiz> result = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            Hanja h = copy.get(i);
            QuestionType t = types[random.nextInt(types.length)];
            result.add(new Quiz(h, t));
        }
        return result;
    }
}