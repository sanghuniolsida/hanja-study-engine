package hanja.ui;

import hanja.domain.Hanja;
import hanja.domain.JsonHanjaRepository;
import hanja.quiz.*;


import java.io.FileDescriptor;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

public class ConsoleApp {
    public static void main(String[] args) {
        forceUtf8Console();

        var repo = new JsonHanjaRepository();
        List<Hanja> pool = repo.findAll();

        var strategy = new RandomQuizStrategy(new java.util.Random(7));
        List<Quiz> quizzes = strategy.selectQuizzes(pool, 5);

        int correct = 0;
        try (Scanner sc = new Scanner(System.in, StandardCharsets.UTF_8)) {
            for (Quiz q : quizzes) {
                System.out.println(q.prompt());
                String ans = sc.hasNextLine() ? sc.nextLine() : null;
                if (ans == null) {
                    System.out.println("\n(입력이 감지되지 않아 세션을 종료합니다)");
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
