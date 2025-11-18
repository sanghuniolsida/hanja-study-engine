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
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class HanjaFXApp extends Application {

    private enum Screen { HOME, QUIZ, CARDS }

    private VBox root;
    private StackPane contentRoot;
    private Label headerTitle;
    private ToggleButton darkToggle;

    private VBox homePane;

    private VBox settingsPane;
    private VBox quizPane;
    private Label quizLabel, timerLabel, counterLabel, levelChip, typeChip;
    private TextField answerField;
    private Button startBtn, submitBtn, exitBtn;
    private ProgressBar timeBar;
    private final DoubleProperty timeProgress = new SimpleDoubleProperty(1.0);

    private List<Quiz> quizzes = List.of();
    private List<Attempt> attempts = new ArrayList<>();
    private int idx = 0, correct = 0, total = 0;
    private Timeline timeline;
    private int remainSec;

    @Override
    public void start(Stage stage) {
        stage.setTitle("한자 학습 엔진 (JavaFX)");

        headerTitle = new Label("한자 학습 엔진");
        headerTitle.getStyleClass().add("title");
        darkToggle = new ToggleButton("Dark");

        Pane headerSpacer = new Pane();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        HBox header = new HBox(10, headerTitle, headerSpacer, darkToggle);
        header.getStyleClass().add("header");

        contentRoot = new StackPane();
        contentRoot.setPadding(new Insets(16));

        root = new VBox(12, header, contentRoot);
        root.setPadding(new Insets(16));

        Scene scene = new Scene(root, 760, 520);
        URL css = getClass().getResource("/ui/styles.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());

        darkToggle.selectedProperty().addListener((obs, off, on) -> {
            if (on) root.getStyleClass().add("dark");
            else    root.getStyleClass().remove("dark");
        });

        stage.setScene(scene);
        stage.show();

        show(Screen.HOME);
    }

    private void show(Screen target) {
        contentRoot.getChildren().clear();
        switch (target) {
            case HOME  -> contentRoot.getChildren().add(buildHome());
            case QUIZ  -> contentRoot.getChildren().add(buildQuizScreen());
            case CARDS -> contentRoot.getChildren().add(buildCardsPlaceholder());
        }
    }

    /* ---------- HOME 화면 ---------- */
    private VBox buildHome() {
        if (homePane != null) return homePane;

        Label title = new Label("한자 학습 엔진");
        title.getStyleClass().add("question");

        Label desc = new Label("원하는 기능을 선택하세요");
        desc.getStyleClass().add("counter");

        Button quizBtn = new Button("한자 퀴즈 시작");
        quizBtn.getStyleClass().add("primary-btn");
        quizBtn.setMaxWidth(Double.MAX_VALUE);

        Button cardsBtn = new Button("낱말 카드 보기");
        cardsBtn.getStyleClass().add("danger-btn");
        cardsBtn.setMaxWidth(Double.MAX_VALUE);

        quizBtn.setOnAction(e -> show(Screen.QUIZ));
        cardsBtn.setOnAction(e -> show(Screen.CARDS));

        VBox buttons = new VBox(10, quizBtn, cardsBtn);
        buttons.setFillWidth(true);

        VBox box = new VBox(12, title, desc, buttons);
        box.getStyleClass().add("card");
        box.setPadding(new Insets(24));
        box.setAlignment(Pos.CENTER_LEFT);

        BorderPane layout = new BorderPane();
        layout.setCenter(box);

        homePane = new VBox(layout);
        return homePane;
    }

    /* ---------- QUIZ 화면 ---------- */
    private VBox buildQuizScreen() {

        levelChip = chip("급수");
        typeChip  = chip("유형");
        counterLabel = new Label("Q 0 / 0");
        counterLabel.getStyleClass().add("counter");

        timeBar = new ProgressBar();
        timeBar.getStyleClass().add("timebar");
        timeBar.setPrefWidth(220);
        timeBar.progressProperty().bind(timeProgress);

        HBox chips = new HBox(8, levelChip, typeChip);
        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox statusStrip = new HBox(12, chips, spacer, counterLabel, timeBar);

        var levelsField = new TextField();
        levelsField.setPromptText("예: 8급 또는 7급,8급 (비우면 전체)");

        var countSpinner = new Spinner<>(1, 200, 20);
        countSpinner.setEditable(true);

        var modeCombo = new ComboBox<Mode>();
        modeCombo.getItems().addAll(Mode.MEANING, Mode.READING, Mode.MIX);
        modeCombo.getSelectionModel().select(Mode.MEANING);

        var timeoutSpinner = new Spinner<>(5, 300, 30);
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
        settingsPane.getStyleClass().add("card");
        settingsPane.setPadding(new Insets(16));

        quizLabel = new Label("여기에 문제가 표시됩니다");
        quizLabel.getStyleClass().add("question");
        timerLabel = new Label("남은 시간: --초");

        answerField = new TextField();
        answerField.getStyleClass().add("text-input");
        answerField.setPromptText("정답을 입력하세요 (exit 입력 시 종료)");

        submitBtn = new Button("제출");
        submitBtn.getStyleClass().add("primary-btn");
        exitBtn = new Button("종료");
        exitBtn.getStyleClass().add("danger-btn");

        HBox actions = new HBox(8, submitBtn, exitBtn);
        quizPane = new VBox(12, quizLabel, timerLabel, answerField, actions);
        quizPane.getStyleClass().add("card");
        quizPane.setPadding(new Insets(16));
        quizPane.setVisible(false);

        answerField.setOnAction(e -> onSubmit(false));
        submitBtn.setOnAction(e -> onSubmit(false));
        exitBtn.setOnAction(e -> finishSession(true));

        root.getScene().setOnKeyPressed(ev -> {
            if (ev.getCode() == KeyCode.ESCAPE && quizPane.isVisible()) finishSession(true);
        });

        startBtn.setOnAction(e -> {
            List<String> levels = parseLevels(levelsField.getText());
            int count = safeInt(countSpinner.getValue(), 20);
            Mode mode = modeCombo.getValue();
            int timeout = safeInt(timeoutSpinner.getValue(), 30);
            SessionConfig cfg = new SessionConfig(levels, count, timeout, mode, null);
            startQuiz(cfg);
        });

        VBox container = new VBox(12, settingsPane, new Separator(), statusStrip, quizPane);
        return container;
    }

    private void startQuiz(SessionConfig cfg) {
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
        idx = 0; correct = 0; total = quizzes.size();

        settingsPane.setDisable(true);
        quizPane.setVisible(true);
        answerField.requestFocus();

        nextQuestion(cfg.timeoutSec());
    }

    /* ---------- 낱말 카드 화면 ---------- */
    private VBox buildCardsPlaceholder() {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(24));
        box.getStyleClass().add("card");

        Label title = new Label("낱말 카드 (준비 중)");
        title.getStyleClass().add("question");
        Label hint = new Label("다음 단계에서 급수 토글 + 카드 플립 + 이전/다음 네비게이션을 추가합니다.");
        hint.getStyleClass().add("counter");

        Button back = new Button("← 홈으로");
        back.setOnAction(e -> show(Screen.HOME));

        box.getChildren().addAll(title, hint, back);
        return box;
    }

    /* ---------- 퀴즈 로직(기존) ---------- */
    private void nextQuestion(long timeoutSec) {
        if (idx >= quizzes.size()) { finishSession(false); return; }

        submitBtn.setDisable(false);
        exitBtn.setDisable(false);
        answerField.setDisable(false);

        Quiz q = quizzes.get(idx);
        quizLabel.setText("[" + q.hanja().getLevel() + "] " + q.prompt());
        levelChip.setText(q.hanja().getLevel());
        typeChip.setText(switch (q.type()) {
            case HANJA_TO_MEANING -> "뜻";
            case HANJA_TO_READING -> "음";
            case MEANING_TO_HANJA -> "한자";
        });
        counterLabel.setText("Q " + (idx + 1) + " / " + total);

        answerField.clear();
        remainSec = (int) timeoutSec;
        timeProgress.set(1.0);
        updateTimerLabel();

        if (timeline != null) timeline.stop();
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), ae -> {
            remainSec--;
            if (timeoutSec > 0) timeProgress.set(Math.max(0, remainSec / (double) timeoutSec));
            updateTimerLabel();
            if (remainSec <= 0) onSubmit(true);
        }));
        timeline.setCycleCount(remainSec);
        timeline.playFromStart();
    }

    private void onSubmit(boolean timeout) {
        if (idx >= quizzes.size()) return;
        submitBtn.setDisable(true); exitBtn.setDisable(true); answerField.setDisable(true);

        Quiz q = quizzes.get(idx);
        String ans = timeout ? Attempt.TIMEOUT : answerField.getText().trim();
        if (timeline != null) timeline.stop();

        if (!timeout && ans.equalsIgnoreCase("exit")) { finishSession(true); return; }

        boolean ok = !timeout && q.isCorrect(ans);
        attempts.add(new Attempt(q, ans, ok));
        if (ok) correct++;

        if (timeout || !ok) { showToast("❌ " + (timeout ? "시간 초과!" : "오답!") + " (정답: " + answerOf(q) + ")"); shakeNode(quizPane); }
        else { showToast("✅ 정답!"); flashGreen(quizPane); }

        idx++;
        nextQuestion(Math.max(5, remainSec)); // 다음 문제는 설정값 재적용 가능
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

        // 초기화 + 홈으로
        quizzes = List.of(); attempts.clear(); idx = 0; correct = 0; total = 0;
        show(Screen.HOME);
    }

    private Label chip(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("chip");
        return l;
    }

    private static List<Quiz> buildQuizzes(List<Hanja> pool, SessionConfig cfg) {
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
        return Arrays.stream(raw.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    private static int safeInt(Integer v, int def) { return v == null ? def : v; }

    private void updateTimerLabel() { timerLabel.setText("남은 시간: " + remainSec + "초"); }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert a = new Alert(type); a.setTitle(title); a.setHeaderText(null); a.setContentText(content); a.showAndWait();
    }

    private void showToast(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText(null); a.setTitle("알림"); a.show();
        new Timeline(new KeyFrame(Duration.millis(800), e -> a.close())).play();
    }

    private void shakeNode(Region node) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(120), node);
        tt.setFromX(0); tt.setByX(10); tt.setCycleCount(4); tt.setAutoReverse(true); tt.play();
    }

    private void flashGreen(Region node) {
        String old = node.getStyle();
        node.setStyle("-fx-background-color: rgba(22,163,74,0.15); -fx-background-radius: 12; -fx-border-radius: 12;");
        new Timeline(new KeyFrame(Duration.millis(250), e -> node.setStyle(old))).play();
    }

    public static void main(String[] args) { launch(args); }
}