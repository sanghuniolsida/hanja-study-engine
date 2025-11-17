package hanja.ui;

import hanja.domain.Hanja;
import hanja.domain.JsonHanjaRepository;
import hanja.quiz.*;
import hanja.util.AsyncLineReader;

import java.io.FileDescriptor;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ConsoleApp {

    private static final long ANSWER_TIMEOUT_SEC = 30; // ★ 30초 제한

    public static void main(String[] args) {
        forceUtf8Console();

        var repo = new JsonHanjaRepository();
        List<Hanja> pool = repo.findAll();

        var strategy = new RandomQuizStrategy(new java.util.Random());
        List<Quiz> quizzes = strategy.selectQuizzes(pool, 5);

        int correct = 0;

        try (AsyncLineReader in = new AsyncLineReader(System.in, StandardCharsets.UTF_8)) {
            for (Quiz q : quizzes) {
                // 급수 표시 + 타임아웃 안내 + exit 안내
                System.out.println("[" + q.hanja().getLevel() + "] " + q.prompt() +
                        "  (제한 " + ANSWER_TIMEOUT_SEC + "초, 종료하려면 'exit')");

                var opt = in.readLine(ANSWER_TIMEOUT_SEC, TimeUnit.SECONDS);

                if (opt.isEmpty()) {
                    // 시간 초과 → 오답 처리
                    System.out.println("⏰ 시간 초과! 오답으로 처리합니다. (정답: " + answerOf(q) + ")\n");
                    continue;
                }

                String ans = opt.get().trim();
                if (ans.equalsIgnoreCase("exit")) {
                    System.out.println("학습을 종료합니다.");
                    break;
                }

                boolean ok = q.isCorrect(ans);
                System.out.println(ok ? "✅ 정답" : "❌ 오답 (정답: " + answerOf(q) + ")");
                if (ok) correct++;
                System.out.println();
            }
        }

        System.out.println("결과: " + correct + " / " + quizzes.size());
    }

    private static void forceUtf8Console() {
        System.setOut(new PrintStream(new java.io.FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(new java.io.FileOutputStream(FileDescriptor.err), true, StandardCharsets.UTF_8));
    }

    private static String answerOf(Quiz q) {
        return switch (q.type()) {
            case HANJA_TO_MEANING -> q.hanja().getMeaning();
            case MEANING_TO_HANJA -> q.hanja().getCharacter();
            case HANJA_TO_READING -> q.hanja().getReading();
        };
    }
}