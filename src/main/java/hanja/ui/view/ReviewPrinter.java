package hanja.ui.view;

import hanja.quiz.Quiz;
import hanja.ui.session.Attempt;

import java.util.List;

public final class ReviewPrinter {

    public void print(List<Attempt> attempts) {
        List<Attempt> wrongs = attempts.stream().filter(a -> !a.correct()).toList();
        System.out.println("\n---- 오답 정리 ----");
        if (wrongs.isEmpty()) {
            System.out.println("🎉 오답 없음! 깔끔합니다.");
            return;
        }
        int idx = 1;
        for (Attempt a : wrongs) {
            Quiz q = a.quiz();
            String typeLabel = switch (q.type()) {
                case HANJA_TO_MEANING -> "뜻";
                case HANJA_TO_READING -> "음";
                case MEANING_TO_HANJA -> "한자";
                case MCQ_TEXT_TO_HANJA -> "객관식(한자)";
                case MCQ_HANJA_TO_TEXT -> "객관식(뜻/음)";
            };
            String expected = answerOf(q);
            String your = Attempt.TIMEOUT.equals(a.userAnswer()) ? "⏰ 시간 초과" : a.userAnswer();

            System.out.printf("%2d) [%s] (%s) %s → 정답: %s | 내 답: %s%n",
                    idx++, q.hanja().getLevel(), typeLabel,
                    questionKey(q), expected, your);
        }
    }

    private String questionKey(Quiz q) {
        return switch (q.type()) {
            case HANJA_TO_MEANING, HANJA_TO_READING -> q.hanja().getCharacter();
            case MEANING_TO_HANJA -> q.hanja().getMeaning();
            case MCQ_HANJA_TO_TEXT -> q.hanja().getCharacter();
            case MCQ_TEXT_TO_HANJA -> q.hanja().getMeaning() + " / " + q.hanja().getReading();
        };
    }

    private String answerOf(Quiz q) {
        return switch (q.type()) {
            case HANJA_TO_MEANING -> q.hanja().getMeaning();
            case MEANING_TO_HANJA -> q.hanja().getCharacter();
            case HANJA_TO_READING -> q.hanja().getReading();
            case MCQ_TEXT_TO_HANJA, MCQ_HANJA_TO_TEXT -> {
                try {
                    yield q.options().get(q.correctIndex());
                } catch (Exception e) {
                    yield "(정답 정보 없음)";
                }
            }
        };
    }
}