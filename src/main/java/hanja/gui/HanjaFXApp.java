package hanja.gui;

import hanja.domain.Hanja;
import hanja.domain.JsonHanjaRepository;
import hanja.quiz.QuestionType;
import hanja.quiz.Quiz;
import hanja.ui.config.SessionConfig;
import hanja.ui.config.SessionConfig.Mode;
import hanja.ui.session.Attempt;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;
import java.util.stream.Collectors;

public class HanjaFXApp extends Application {

    private TextField levelsField;
    private Spinner<Integer> countSpinner;
    private ComboBox<Mode> modeCombo;
    private Spinner<Integer> timeoutSpinner;
    private Button startBtn;

    private Label quizLabel;
    private Label timerLabel;
    private TextField answerField;
    private Button submitBtn;
    private Button exitBtn;

    private VBox settingsPane;
    private VBox quizPane;

    private List<Quiz> quizzes = List.of();
    private List<Attempt> attempts = new ArrayList<>();
    private int idx = 0;
    private int correct = 0;
    private Timeline timeline;
    private int remainSec;

    @Override
    public void start(Stage stage) {
        stage.setTitle("한자 학습 엔진 (JavaFX)");

        // 설정 영역
        levelsField = new TextField();
        levelsField.setPromptText("예: 8급 또는 7급,8급 (비우면 전체)");

        countSpinner = new Spinner<>(1, 200, 20);
        countSpinner.setEditable(true);

        modeCombo = new ComboBox<>();
        modeCombo.getItems().addAll(Mode.MEANING, Mode.READING, Mode.MIX);
        modeCombo.getSelectionModel().select(Mode.MEANING);

        timeoutSpinner = new Spinner<>(5, 300, 30);
        timeoutSpinner.setEditable(true);

        startBtn = new Button("시작");

        GridPane grid = new GridPane();
        grid.setHgap(8); grid.setVgap(8);
        grid.add(new Label("급수"), 0, 0);      grid.add(levelsField, 1, 0);
        grid.add(new Label("문항 수"), 0, 1);    grid.add(countSpinner, 1, 1);
        grid.add(new Label("모드"), 0, 2);      grid.add(modeCombo, 1, 2);
        grid.add(new Label("제한(초)"), 0, 3);  grid.add(timeoutSpinner, 1, 3);
        grid.add(startBtn, 1, 4);

        settingsPane = new VBox(10, new Label("세션 설정"), grid);
        settingsPane.setPadding(new Insets(16));
        settingsPane.setStyle("-fx-background-color: #fafafa; -fx-border-color: #ddd; -fx-border-radius: 8; -fx-background-radius: 8;");

        // 문제 영역
        quizLabel = new Label("여기에 문제가 표시됩니다");
        quizLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");
        timerLabel = new Label("남은 시간: --초");
        answerField = new TextField();
        answerField.setPromptText("정답을 입력하세요 (exit 입력 시 종료)");
        submitBtn = new Button("제출");
        exitBtn = new Button("종료");

        HBox actions = new HBox(8, submitBtn, exitBtn);
        quizPane = new VBox(12, quizLabel, timerLabel, answerField, actions);
        quizPane.setPadding(new Insets(16));
        quizPane.setVisible(false); // 시작 전 비가시화

        VBox root = new VBox(16, settingsPane, new Separator(), quizPane);
        root.setPadding(new Insets(16));
        Scene scene = new Scene(root, 640, 420);
        stage.setScene(scene);
        stage.show();

        startBtn.setOnAction(e -> onStart());
        submitBtn.setOnAction(e -> onSubmit(false));
        exitBtn.setOnAction(e -> {
            finishSession(true);
        });
        answerField.setOnAction(e -> onSubmit(false)); // Enter 제출
    }

    private void onStart() {
        List<String> levels = parseLevels(levelsField.getText());
        int count = safeInt(countSpinner.getValue(), 20);
        Mode mode = modeCombo.getValue();
        int timeout = safeInt(timeoutSpinner.getValue(), 30);

        SessionConfig cfg = new SessionConfig(levels, count, timeout, mode, null);

        var repo = new JsonHanjaRepository();
        List<Hanja> pool = repo.findAll();
        if (!cfg.levels().isEmpty()) {
            Set<String> wanted = new HashSet<>(cfg.levels());
            pool = pool.stream().filter(h -> wanted.contains(h.getLevel())).collect(Collectors.toList());
        }
        if (pool.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "데이터 없음", "선택한 급수의 한자 데이터가 없습니다.");
            return;
        }

        quizzes = buildQuizzes(pool, cfg);
        attempts = new ArrayList<>(quizzes.size());
        idx = 0;
        correct = 0;

        settingsPane.setDisable(true);
        quizPane.setVisible(true);
        answerField.requestFocus();

        nextQuestion(cfg.timeoutSec());
    }

    private void nextQuestion(long timeoutSec) {
        if (idx >= quizzes.size()) {
            finishSession(false);
            return;
        }
        Quiz q = quizzes.get(idx);
        quizLabel.setText("[" + q.hanja().getLevel() + "] " + q.prompt());
        answerField.clear();
        remainSec = (int) timeoutSec;
        updateTimerLabel();

        if (timeline != null) timeline.stop();
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), ae -> {
            remainSec--;
            updateTimerLabel();
            if (remainSec <= 0) {
                onSubmit(true);
            }
        }));
        timeline.setCycleCount(remainSec);
        timeline.playFromStart();
    }

    private void updateTimerLabel() {
        timerLabel.setText("남은 시간: " + remainSec + "초");
    }

    private void onSubmit(boolean timeout) {
        if (idx >= quizzes.size()) return;
        Quiz q = quizzes.get(idx);

        String ans = timeout ? Attempt.TIMEOUT : answerField.getText().trim();
        if (timeline != null) timeline.stop();

        if (!timeout && ans.equalsIgnoreCase("exit")) {
            finishSession(true);
            return;
        }

        boolean ok = !timeout && q.isCorrect(ans);
        attempts.add(new Attempt(q, ans, ok));
        if (ok) correct++;

        if (timeout) {
            showToast("⏰ 시간 초과! (정답: " + answerOf(q) + ")");
        } else if (ok) {
            showToast("✅ 정답!");
        } else {
            showToast("❌ 오답! (정답: " + answerOf(q) + ")");
        }

        idx++;
        nextQuestion(timeoutSpinner.getValue());
    }

    private void finishSession(boolean exitedEarly) {
        if (timeline != null) timeline.stop();

        StringBuilder sb = new StringBuilder();
        List<Attempt> wrongs = attempts.stream().filter(a -> !a.correct()).toList();

        sb.append("결과: ").append(correct).append(" / ").append(attempts.size());
        if (exitedEarly) sb.append(" (중도 종료)");
        sb.append("\n\n");

        if (wrongs.isEmpty()) {
            sb.append("🎉 오답 없음! 깔끔합니다.");
        } else {
            sb.append("---- 오답 정리 ----\n");
            int i = 1;
            for (Attempt a : wrongs) {
                Quiz q = a.quiz();
                String typeLabel = switch (q.type()) {
                    case HANJA_TO_MEANING -> "뜻";
                    case HANJA_TO_READING -> "음";
                    case MEANING_TO_HANJA -> "한자";
                };
                String your = Attempt.TIMEOUT.equals(a.userAnswer()) ? "⏰ 시간 초과" : a.userAnswer();
                sb.append(String.format("%2d) [%s] (%s) %s → 정답: %s | 내 답: %s%n",
                        i++, q.hanja().getLevel(), typeLabel, questionKey(q), answerOf(q), your));
            }
        }

        showAlert(Alert.AlertType.INFORMATION, "세션 요약", sb.toString());

        settingsPane.setDisable(false);
        quizPane.setVisible(false);
        attempts.clear();
        quizzes = List.of();
        idx = 0; correct = 0;
    }

    // ----- helpers -----
    private static List<Quiz> buildQuizzes(List<Hanja> pool, SessionConfig cfg) {
        // 중복 없이 N개
        List<Hanja> shuffled = new ArrayList<>(pool);
        Collections.shuffle(shuffled, new Random());
        List<Hanja> chosen = shuffled.subList(0, Math.min(cfg.count(), shuffled.size()));

        Random rng = new Random();
        List<Quiz> out = new ArrayList<>(chosen.size());
        for (Hanja h : chosen) {
            QuestionType type = switch (cfg.mode()) {
                case MEANING -> QuestionType.HANJA_TO_MEANING;
                case READING -> QuestionType.HANJA_TO_READING;
                case MIX     -> (rng.nextBoolean() ? QuestionType.HANJA_TO_MEANING : QuestionType.HANJA_TO_READING);
            };
            out.add(new Quiz(h, type));
        }
        return out;
    }

    private static String answerOf(Quiz q) {
        return switch (q.type()) {
            case HANJA_TO_MEANING -> q.hanja().getMeaning();
            case MEANING_TO_HANJA -> q.hanja().getCharacter();
            case HANJA_TO_READING -> q.hanja().getReading();
        };
    }

    private static String questionKey(Quiz q) {
        return switch (q.type()) {
            case HANJA_TO_MEANING, HANJA_TO_READING -> q.hanja().getCharacter();
            case MEANING_TO_HANJA -> q.hanja().getMeaning();
        };
    }

    private static List<String> parseLevels(String raw) {
        if (raw == null || raw.isBlank()) return List.of();
        return Arrays.stream(raw.split(","))
                .map(String::trim).filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private static int safeInt(Integer v, int def) { return v == null ? def : v; }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(content);
        a.showAndWait();
    }

    private void showToast(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.setTitle("알림");
        a.show();
        new Timeline(new KeyFrame(Duration.millis(800), e -> a.close())).play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}