package hanja.quiz;

import hanja.domain.Hanja;
import hanja.study.UserProgress;

import java.util.*;

public class MistakeFirstStrategy implements QuizStrategy {

    private final UserProgress progress;
    private final Random random;

    public MistakeFirstStrategy(UserProgress progress) {
        this(progress, new Random());
    }

    public MistakeFirstStrategy(UserProgress progress, Random random) {
        this.progress = Objects.requireNonNull(progress, "progress");
        this.random = Objects.requireNonNull(random, "random");
    }

    @Override
    public List<Quiz> selectQuizzes(List<Hanja> pool, int count) {
        if (pool == null || pool.isEmpty() || count <= 0) return List.of();

        List<Hanja> pending = new ArrayList<>();
        List<Hanja> others  = new ArrayList<>();
        for (Hanja h : pool) {
            if (progress.needsReview(h)) pending.add(h);
            else others.add(h);
        }

        Collections.shuffle(pending, random);
        Collections.shuffle(others, random);

        int n = Math.min(count, pool.size());
        List<Hanja> chosen = new ArrayList<>(n);
        addSome(chosen, pending, n);
        addSome(chosen, others, n);

        QuestionType[] types = QuestionType.values();
        List<Quiz> result = new ArrayList<>(chosen.size());
        for (Hanja h : chosen) {
            QuestionType t = types[random.nextInt(types.length)];
            result.add(new Quiz(h, t));
        }
        return result;
    }

    private void addSome(List<Hanja> out, List<Hanja> src, int limit) {
        for (Hanja h : src) {
            if (out.size() >= limit) break;
            out.add(h);
        }
    }
}