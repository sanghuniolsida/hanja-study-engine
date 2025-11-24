package hanja.quiz;

import hanja.domain.Hanja;

import java.util.*;
import java.util.stream.Collectors;

public final class McqQuizFactory {

    private McqQuizFactory() {}

    public enum Kind {
        TEXT_TO_HANJA,
        HANJA_TO_TEXT
    }

    /**
     * @param levelFiltered  화면/세션 등으로 1차 필터된 후보(없으면 all 사용)
     * @param all            오답 샘플링에 사용할 전체 데이터(레벨 무관)
     * @param count          생성할 문제 수
     * @param kind           MCQ 유형
     * @param rng            재현성 있는 무작위
     */
    public static List<Quiz> build(List<Hanja> levelFiltered,
                                   List<Hanja> all,
                                   int count,
                                   Kind kind,
                                   Random rng) {
        Objects.requireNonNull(all, "all");
        Objects.requireNonNull(rng, "rng");
        if (all.size() < 4) return List.of();

        List<Hanja> base = (levelFiltered == null || levelFiltered.isEmpty()) ? all : levelFiltered;

        List<Hanja> candidates = new ArrayList<>(base);
        Collections.shuffle(candidates, rng);

        int n = Math.min(count, candidates.size());
        List<Quiz> out = new ArrayList<>(n);

        for (int i = 0; i < n; i++) {
            Hanja target = candidates.get(i);

            switch (kind) {
                case TEXT_TO_HANJA -> {
                    String correct = target.getCharacter();

                    List<String> pool = all.stream()
                            .map(Hanja::getCharacter)
                            .filter(Objects::nonNull)
                            .filter(ch -> !ch.equals(correct))
                            .distinct()
                            .collect(Collectors.toCollection(ArrayList::new));

                    if (pool.size() < 3) continue;

                    Collections.shuffle(pool, rng);
                    List<String> options = new ArrayList<>(4);
                    options.add(correct);
                    options.addAll(pool.subList(0, 3));
                    Collections.shuffle(options, rng);

                    int correctIdx = options.indexOf(correct);
                    String prompt = "뜻: " + safe(target.getMeaning()) + " / 음: " + safe(target.getReading());

                    out.add(Quiz.mcq(
                            target,
                            QuestionType.MCQ_TEXT_TO_HANJA,
                            prompt,
                            options,
                            correctIdx
                    ));
                }
                case HANJA_TO_TEXT -> {
                    String correct = formatReadingMeaning(target);

                    List<String> pool = all.stream()
                            .map(McqQuizFactory::formatReadingMeaning)
                            .filter(Objects::nonNull)
                            .filter(s -> !s.equals(correct))
                            .distinct()
                            .collect(Collectors.toCollection(ArrayList::new));

                    if (pool.size() < 3) continue;

                    Collections.shuffle(pool, rng);
                    List<String> options = new ArrayList<>(4);
                    options.add(correct);
                    options.addAll(pool.subList(0, 3));
                    Collections.shuffle(options, rng);

                    int correctIdx = options.indexOf(correct);
                    String prompt = target.getCharacter();

                    out.add(Quiz.mcq(
                            target,
                            QuestionType.MCQ_HANJA_TO_TEXT,
                            prompt,
                            options,
                            correctIdx
                    ));
                }
            }
        }

        return out;
    }

    private static String safe(String s) {
        return (s == null) ? "" : s;
    }

    private static String formatReadingMeaning(Hanja h) {
        if (h == null) return null;
        return safe(h.getReading()) + " · " + safe(h.getMeaning());
    }
}