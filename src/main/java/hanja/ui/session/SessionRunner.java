package hanja.ui.session;

import hanja.domain.Hanja;
import hanja.quiz.QuestionType;
import hanja.quiz.Quiz;
import hanja.ui.config.SessionConfig;
import hanja.ui.config.SessionConfig.Mode;
import hanja.util.AsyncLineReader;
import hanja.util.TimedPrompt;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


public final class SessionRunner {

    public SessionResult run(List<Hanja> all, SessionConfig cfg) {
        List<Hanja> pool = filterByLevels(all, cfg.levels());
        if (pool.isEmpty()) {
            System.out.println("선택한 급수에 해당하는 한자 데이터가 없습니다. hanja-data.json을 확인하세요.");
            return new SessionResult(List.of(), 0, false);
        }

        Random rng = cfg.seed() != null ? new Random(cfg.seed()) : new Random();

        int n = Math.min(cfg.count(), pool.size());
        List<Hanja> shuffled = new ArrayList<>(pool);
        Collections.shuffle(shuffled, rng);
        List<Hanja> chosen = shuffled.subList(0, n);

        List<Quiz> quizzes = buildQuizzes(chosen, cfg.mode(), rng);

        int correct = 0;
        boolean exited = false;
        List<Attempt> attempts = new ArrayList<>(quizzes.size());

        try (AsyncLineReader in = new AsyncLineReader(System.in, StandardCharsets.UTF_8)) {
            TimedPrompt countdown = new TimedPrompt(System.out, 100);

            for (Quiz q : quizzes) {
                System.out.println("[" + q.hanja().getLevel() + "] " + q.prompt());

                var line = countdown.readLineWithCountdown(in, cfg.timeoutSec(), "입력 또는 'exit' → ");
                if (line.isEmpty()) {
                    attempts.add(new Attempt(q, Attempt.TIMEOUT, false));
                    System.out.println("⏰ 시간 초과! 오답으로 처리합니다. (정답: " + answerOf(q) + ")\n");
                    continue;
                }

                String ans = line.get().trim();
                if (ans.equalsIgnoreCase("exit")) {
                    System.out.println("학습을 종료합니다.");
                    exited = true;
                    break;
                }

                boolean ok = q.isCorrect(ans);
                attempts.add(new Attempt(q, ans, ok));
                if (ok) System.out.println("✅ 정답");
                else    System.out.println("❌ 오답 (정답: " + answerOf(q) + ")");
                if (ok) correct++;
                System.out.println();
            }
        }

        return new SessionResult(List.copyOf(attempts), correct, exited);
    }

    private List<Hanja> filterByLevels(List<Hanja> all, List<String> levels) {
        if (levels == null || levels.isEmpty()) return all;
        var wanted = new HashSet<>(levels);
        return all.stream().filter(h -> wanted.contains(h.getLevel())).collect(Collectors.toList());
    }

    private List<Quiz> buildQuizzes(List<Hanja> chosen, Mode mode, Random rng) {
        List<Quiz> qs = new ArrayList<>(chosen.size());
        for (Hanja h : chosen) {
            QuestionType type;
            if (mode == Mode.MEANING) {
                type = QuestionType.HANJA_TO_MEANING;
            } else if (mode == Mode.READING) {
                type = QuestionType.HANJA_TO_READING;
            } else {
                boolean pickMeaning = rng.nextBoolean();
                if (pickMeaning) type = QuestionType.HANJA_TO_MEANING;
                else type = QuestionType.HANJA_TO_READING;
            }
            qs.add(new Quiz(h, type));
        }
        return qs;
    }

    private String answerOf(Quiz q) {
        return switch (q.type()) {
            case HANJA_TO_MEANING -> q.hanja().getMeaning();
            case MEANING_TO_HANJA -> q.hanja().getCharacter();
            case HANJA_TO_READING -> q.hanja().getReading();case MCQ_TEXT_TO_HANJA, MCQ_HANJA_TO_TEXT -> {
                try {
                    yield q.options().get(q.correctIndex());
                } catch (Exception e) {
                    yield "(정답 정보 없음)";
                }
            }
        };
    }
}