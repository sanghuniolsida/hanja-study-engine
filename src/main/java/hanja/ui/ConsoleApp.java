package hanja.ui;

import hanja.domain.Hanja;
import hanja.domain.JsonHanjaRepository;
import hanja.quiz.*;

import java.io.FileDescriptor;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class ConsoleApp {

    enum Mode { MEANING, READING, MIX }
    record CliOptions(int count, List<String> levels, Long seed, Mode mode) {}

    public static void main(String[] args) {
        forceUtf8Console();

        CliOptions opt = parseOptions(args);

        var repo = new JsonHanjaRepository();
        List<Hanja> pool = repo.findAll();

        if (!opt.levels().isEmpty()) {
            Set<String> wanted = new HashSet<>(opt.levels());
            pool = pool.stream()
                    .filter(h -> wanted.contains(h.getLevel()))
                    .collect(Collectors.toList());
        }
        if (pool.isEmpty()) {
            System.out.println("선택한 급수에 해당하는 한자 데이터가 없습니다. (--levels 5급,6급,7급,8급 등)");
            return;
        }

        int n = Math.min(opt.count(), pool.size());

        Random rng = opt.seed() != null ? new Random(opt.seed()) : new Random();
        List<Hanja> shuffled = new ArrayList<>(pool);
        Collections.shuffle(shuffled, rng);
        List<Hanja> chosen = shuffled.subList(0, n);

        // 모드에 따라 문제 타입 결정
        List<Quiz> quizzes = new ArrayList<>(n);
        for (Hanja h : chosen) {
            QuestionType type = switch (opt.mode()) {
                case MEANING -> QuestionType.HANJA_TO_MEANING;
                case READING -> QuestionType.HANJA_TO_READING;
                case MIX -> (rng.nextBoolean()
                        ? QuestionType.HANJA_TO_MEANING
                        : QuestionType.HANJA_TO_READING);
            };
            quizzes.add(new Quiz(h, type));
        }

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

    private static CliOptions parseOptions(String[] args) {
        int count = 20;                 // 기본 20문항
        List<String> levels = List.of();// 기본: 전체 급수
        Long seed = null;               // 재현성 필요 시
        Mode mode = Mode.MIX;           // 기본: 뜻/음 섞어서

        for (int i = 0; args != null && i < args.length; i++) {
            switch (args[i]) {
                case "--help", "-h" -> {
                    printHelp();
                    System.exit(0);
                }
                case "--count", "-n" -> {
                    if (i + 1 < args.length) {
                        try { count = Math.max(1, Integer.parseInt(args[++i])); } catch (NumberFormatException ignore) {}
                    }
                }
                case "--levels", "--level", "-l" -> {
                    if (i + 1 < args.length) {
                        levels = Arrays.stream(args[++i].split(","))
                                .map(String::trim).filter(s -> !s.isEmpty()).toList();
                    }
                }
                case "--seed" -> {
                    if (i + 1 < args.length) {
                        try { seed = Long.parseLong(args[++i]); } catch (NumberFormatException ignore) {}
                    }
                }
                case "--mode" -> {
                    if (i + 1 < args.length) {
                        String m = args[++i].trim().toLowerCase();
                        mode = switch (m) {
                            case "meaning" -> Mode.MEANING;
                            case "reading" -> Mode.READING;
                            case "mix"     -> Mode.MIX;
                            default        -> mode;
                        };
                    }
                }
                default -> { /* ignore */ }
            }
        }
        return new CliOptions(count, levels, seed, mode);
    }

    private static void printHelp() {
        System.out.println("""
            사용법: ./gradlew run --args="옵션..."
              옵션:
                --levels, -l   "5급,6급"    선택 급수(쉼표 구분). 생략 시 전체
                --count,  -n   20           출제 문항 수(기본 20)
                --mode         meaning|reading|mix (기본 mix)
                --seed         42           시드 고정(재현 가능한 랜덤)

              예시:
                ./gradlew run --args="--levels 8급 --count 20 --mode meaning"
                ./gradlew run --args="--levels 5급,6급 -n 20 --mode reading"
                ./gradlew run --args="--levels 7급,8급 -n 20 --mode mix --seed 42"
            """);
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