package hanja.ui;

import hanja.domain.Hanja;
import hanja.domain.JsonHanjaRepository;
import hanja.quiz.*;
import hanja.util.AsyncLineReader;
import hanja.util.TimedPrompt;

import java.io.FileDescriptor;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class ConsoleApp {

    enum Mode { MEANING, READING, MIX }
    private static final long ANSWER_TIMEOUT_SEC = 30;
    public static void main(String[] args) {
        forceUtf8Console();

        // === 초기 프롬프트: 급수/문항/모드 선택 ===
        List<String> levels;
        int count = 20;
        Mode mode = Mode.MEANING;

        try (Scanner sc = new Scanner(System.in, StandardCharsets.UTF_8)) {
            System.out.print("학습 급수 입력 (예: 8급 또는 7급,8급, 비우면 전체): ");
            String lvRaw = sc.hasNextLine() ? sc.nextLine().trim() : "";
            levels = lvRaw.isEmpty()
                    ? List.of()
                    : Arrays.stream(lvRaw.split(","))
                    .map(String::trim).filter(s -> !s.isEmpty()).toList();

            System.out.print("문항 수 입력 (기본 20): ");
            if (sc.hasNextLine()) {
                String nRaw = sc.nextLine().trim();
                if (!nRaw.isEmpty()) {
                    try { count = Math.max(1, Integer.parseInt(nRaw)); } catch (NumberFormatException ignore) {}
                }
            }

            System.out.print("모드 선택 [1] 뜻  [2] 음(독음)  [3] 섞어서  (기본 1): ");
            if (sc.hasNextLine()) {
                String mRaw = sc.nextLine().trim();
                mode = switch (mRaw) {
                    case "2" -> Mode.READING;
                    case "3" -> Mode.MIX;
                    default  -> Mode.MEANING;
                };
            }
        }

        var repo = new JsonHanjaRepository();
        List<Hanja> pool = repo.findAll();
        if (!levels.isEmpty()) {
            Set<String> wanted = new HashSet<>(levels);
            pool = pool.stream()
                    .filter(h -> wanted.contains(h.getLevel()))
                    .collect(Collectors.toList());
        }
        if (pool.isEmpty()) {
            System.out.println("선택한 급수에 해당하는 한자 데이터가 없습니다. hanja-data.json을 확인하세요.");
            return;
        }

        int n = Math.min(count, pool.size());
        List<Hanja> shuffled = new ArrayList<>(pool);
        Collections.shuffle(shuffled, new Random());
        List<Hanja> chosen = shuffled.subList(0, n);

        // === 모드에 따라 문제 타입 결정 ===
        List<Quiz> quizzes = new ArrayList<>(n);
        Random rng = new Random();
        for (Hanja h : chosen) {
            QuestionType type = switch (mode) {
                case MEANING -> QuestionType.HANJA_TO_MEANING;
                case READING -> QuestionType.HANJA_TO_READING;
                case MIX     -> (rng.nextBoolean()
                        ? QuestionType.HANJA_TO_MEANING
                        : QuestionType.HANJA_TO_READING);
            };
            quizzes.add(new Quiz(h, type));
        }

        int correct = 0;
        try (AsyncLineReader in = new AsyncLineReader(System.in, StandardCharsets.UTF_8)) {
            TimedPrompt countdown = new TimedPrompt(System.out, 100);

            for (Quiz q : quizzes) {
                System.out.println("[" + q.hanja().getLevel() + "] " + q.prompt());
                var opt = countdown.readLineWithCountdown(in, ANSWER_TIMEOUT_SEC, "입력 또는 'exit' → ");

                if (opt.isEmpty()) {
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