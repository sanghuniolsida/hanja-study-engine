package hanja.gui;

import hanja.domain.Hanja;
import hanja.domain.JsonHanjaRepository;
import hanja.quiz.QuestionType;
import hanja.quiz.Quiz;
import hanja.ui.config.SessionConfig;
import hanja.ui.config.SessionConfig.Mode;
import hanja.ui.session.Attempt;
import hanja.storage.CardState;
import hanja.storage.CardStateRepository;
import hanja.storage.FileCardStateRepository;

import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
    private TextField levelsField;
    private Spinner<Integer> countSpinner;
    private ComboBox<Mode> modeCombo;
    private Spinner<Integer> timeoutSpinner;
    private Button startBtn, submitBtn, exitBtn;
    private ProgressBar timeBar;
    private final DoubleProperty timeProgress = new SimpleDoubleProperty(1.0);
    private TextField answerField;

    private List<Quiz> quizzes = List.of();
    private List<Attempt> attempts = new ArrayList<>();
    private int idx = 0, correct = 0, total = 0;
    private Timeline timeline;
    private int remainSec;

    private VBox cardsRoot;
    private ComboBox<String> levelCombo;
    private StackPane cardArea;
    private StackPane cardStack;
    private Button prevBtn, nextBtn, homeBtn;
    private Button quizHomeBtn;
    private Label cardsCounter;

    private List<Hanja> allData = List.of();
    private List<String> availableLevels = List.of();
    private List<Hanja> baseCardList = List.of();
    private List<Hanja> cardList = List.of();
    private int cardIndex = 0;
    private boolean showingBack = false;
    private Node frontNode, backNode;

    private final CardStateRepository cardRepo = new FileCardStateRepository();

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

        Scene scene = new Scene(root, 820, 600);
        URL css = getClass().getResource("/ui/styles.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());

        darkToggle.selectedProperty().addListener((obs, off, on) -> {
            if (on) root.getStyleClass().add("dark");
            else    root.getStyleClass().remove("dark");
        });

        stage.setScene(scene);
        stage.show();

        scene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleGlobalKeys);

        show(Screen.HOME);
    }

    private void show(Screen target) {
        contentRoot.getChildren().clear();
        switch (target) {
            case HOME  -> contentRoot.getChildren().add(buildHome());
            case QUIZ  -> contentRoot.getChildren().add(buildQuizScreen());
            case CARDS -> contentRoot.getChildren().add(buildCardsScreen());
        }
    }

    private VBox buildHome() {
        if (homePane != null) return homePane;

        Label title = new Label("한자 쏙!쏙!");
        title.getStyleClass().add("question");

        Label desc = new Label("원하는 학습을 선택하세요");
        desc.getStyleClass().add("counter");

        VBox hero = new VBox(8, title, desc);
        hero.getStyleClass().add("card");
        hero.setPadding(new Insets(24));
        hero.setAlignment(Pos.CENTER_LEFT);

        Button quizBtn = new Button("한자 퀴즈 시작");
        quizBtn.getStyleClass().add("primary-btn");
        quizBtn.setMaxWidth(Double.MAX_VALUE);

        Button cardsBtn = new Button("낱말 카드 보기");
        cardsBtn.getStyleClass().add("danger-btn");
        cardsBtn.setMaxWidth(Double.MAX_VALUE);

        quizBtn.setOnAction(e -> show(Screen.QUIZ));
        cardsBtn.setOnAction(e -> show(Screen.CARDS));

        HBox buttons = new HBox(12, quizBtn, cardsBtn);
        HBox.setHgrow(quizBtn, Priority.ALWAYS);
        HBox.setHgrow(cardsBtn, Priority.ALWAYS);

        VBox chooser = new VBox(12, buttons);
        chooser.getStyleClass().add("card");
        chooser.setPadding(new Insets(24));
        chooser.setAlignment(Pos.CENTER_LEFT);

        VBox container = new VBox(16, hero, chooser);

        BorderPane layout = new BorderPane();
        layout.setCenter(container);

        homePane = new VBox(layout);
        return homePane;
    }

    private VBox buildQuizScreen() {
        quizHomeBtn = new Button("홈으로");
        quizHomeBtn.getStyleClass().add("danger-btn");
        quizHomeBtn.setOnAction(e -> {
            if (timeline != null) timeline.stop();
            show(Screen.HOME);
        });

        Pane topSpacer = new Pane();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);
        Label quizTitle = new Label("퀴즈");
        quizTitle.getStyleClass().add("title");
        HBox topBar = new HBox(10, quizTitle, topSpacer, quizHomeBtn);
        topBar.setAlignment(Pos.CENTER_LEFT);

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

        levelsField = new TextField();
        levelsField.setPromptText("예: 8급 또는 7급,8급 (비우면 전체)");

        answerField = new TextField();
        answerField.getStyleClass().add("text-input");
        answerField.setPromptText("정답을 입력하세요 (exit 입력 시 종료)");

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
        grid.add(new Label("급수"), 0, 0);      grid.add(levelsField,    1, 0);
        grid.add(new Label("문항 수"), 0, 1);    grid.add(countSpinner,   1, 1);
        grid.add(new Label("모드"), 0, 2);      grid.add(modeCombo,      1, 2);
        grid.add(new Label("제한(초)"), 0, 3);  grid.add(timeoutSpinner, 1, 3);
        grid.add(startBtn, 1, 4);

        settingsPane = new VBox(10, new Label("세션 설정"), grid);
        settingsPane.getStyleClass().add("card");
        settingsPane.setPadding(new Insets(16));

        quizLabel = new Label("여기에 문제가 표시됩니다");
        quizLabel.getStyleClass().add("question");
        timerLabel = new Label("남은 시간: --초");

        submitBtn = new Button("제출"); submitBtn.getStyleClass().add("primary-btn");
        exitBtn   = new Button("종료");  exitBtn.getStyleClass().add("danger-btn");

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

        return new VBox(12, topBar, settingsPane, new Separator(), statusStrip, quizPane);
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
        nextQuestion(Math.max(5, remainSec));
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

        quizzes = List.of(); attempts.clear(); idx = 0; correct = 0; total = 0;
        show(Screen.HOME);
    }

    private VBox buildCardsScreen() {
        if (cardsRoot != null) return cardsRoot;

        var repo = new JsonHanjaRepository();
        allData = repo.findAll();
        availableLevels = allData.stream()
                .map(Hanja::getLevel)
                .filter(lv -> lv != null && !lv.isBlank())
                .distinct()
                .sorted(Comparator.naturalOrder())
                .toList();

        levelCombo = new ComboBox<>();
        levelCombo.getItems().add("전체");
        levelCombo.getItems().addAll(availableLevels);
        levelCombo.getSelectionModel().selectFirst();
        levelCombo.setOnAction(e -> updateCardsForSelectedLevel());

        homeBtn = new Button("홈으로");
        homeBtn.getStyleClass().add("danger-btn");
        homeBtn.setOnAction(e -> { saveCardState(); show(Screen.HOME); });

        Pane topSpacer = new Pane();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);
        HBox topBar = new HBox(10, new Label("급수:"), levelCombo, topSpacer, homeBtn);
        topBar.setAlignment(Pos.CENTER_LEFT);

        cardStack = new StackPane();
        cardStack.setMinSize(300, 220);
        cardStack.setPrefSize(480, 320);
        cardStack.setMaxWidth(560);
        cardStack.getStyleClass().add("card");
        cardStack.setPadding(new Insets(24));
        cardStack.setOnMouseClicked(e -> flipCard());

        frontNode = buildFront(null);
        backNode  = buildBack(null);
        cardStack.getChildren().setAll(frontNode, backNode);
        backNode.setVisible(false);

        prevBtn = new Button("← 이전");
        nextBtn = new Button("다음 →");
        prevBtn.setOnAction(e -> showCard(cardIndex - 1, true));
        nextBtn.setOnAction(e -> showCard(cardIndex + 1, true));

        cardsCounter = new Label("0 / 0");
        cardsCounter.getStyleClass().add("counter");

        Pane navSpacer = new Pane();
        HBox.setHgrow(navSpacer, Priority.ALWAYS);
        HBox nav = new HBox(8, cardsCounter, navSpacer, prevBtn, nextBtn);
        nav.setAlignment(Pos.CENTER_LEFT);

        cardArea = new StackPane(cardStack);

        VBox layout = new VBox(12, new Label("낱말 카드"), topBar, cardArea, nav);
        ((Label)layout.getChildren().get(0)).getStyleClass().add("title");
        layout.setPadding(new Insets(16));

        cardsRoot = new VBox(layout);
        cardsRoot.setFillWidth(true);

        updateCardsForSelectedLevel();

        return cardsRoot;
    }

    private void updateCardsForSelectedLevel() {
        String selected = levelCombo.getSelectionModel().getSelectedItem();
        if (selected == null || selected.equals("전체")) {
            baseCardList = new ArrayList<>(allData);
        } else {
            baseCardList = allData.stream()
                    .filter(h -> selected.equals(h.getLevel()))
                    .collect(Collectors.toList());
        }
        baseCardList.sort(Comparator.comparing(Hanja::getCharacter));
        cardList = baseCardList;
        cardIndex = Math.min(cardIndex, Math.max(0, cardList.size() - 1));
        showingBack = false;

        CardState st = cardRepo.load();
        String last = st.lastCharacter();
        if (last != null) {
            int found = indexOfCharacter(cardList, last);
            if (found >= 0) cardIndex = found;
        }

        refreshCard();
        updateNavButtons();
    }

    private void refreshCard() {
        if (cardList.isEmpty()) {
            cardsCounter.setText("0 / 0");
            prevBtn.setDisable(true); nextBtn.setDisable(true);
            frontNode = buildFront(null);
            backNode  = buildBack(null);
            cardStack.getChildren().setAll(frontNode, backNode);
            backNode.setVisible(false);
            return;
        }

        Hanja h = cardList.get(cardIndex);
        frontNode = buildFront(h);
        backNode  = buildBack(h);
        cardStack.getChildren().setAll(frontNode, backNode);
        frontNode.setVisible(!showingBack);
        backNode.setVisible(showingBack);

        cardsCounter.setText((cardIndex + 1) + " / " + cardList.size());
    }

    private void showCard(int nextIndex, boolean animate) {
        if (cardList.isEmpty()) return;
        if (nextIndex < 0 || nextIndex >= cardList.size()) return;
        cardIndex = nextIndex;
        showingBack = false;
        if (animate) slide(cardStack);
        refreshCard();
        updateNavButtons();
        saveCardState();
    }

    private void updateNavButtons() {
        prevBtn.setDisable(cardIndex <= 0);
        nextBtn.setDisable(cardIndex >= cardList.size() - 1);
    }

    private Node buildFront(Hanja h) {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER);
        Label lv = new Label(h == null ? "-" : "[" + h.getLevel() + "]");
        lv.getStyleClass().add("counter");
        Label ch = new Label(h == null ? "—" : h.getCharacter());
        ch.setStyle("-fx-font-size: 64px; -fx-font-weight: bold;");
        Label hint = new Label("카드를 클릭하면 뜻/음을 보여줍니다");
        hint.getStyleClass().add("counter");
        box.getChildren().addAll(lv, ch, hint);
        return box;
    }

    private Node buildBack(Hanja h) {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        Label ch = new Label(h == null ? "—" : h.getCharacter());
        ch.setStyle("-fx-font-size: 42px; -fx-font-weight: bold;");
        Label reading = new Label(h == null ? "-" : "음: " + h.getReading());
        Label meaning = new Label(h == null ? "-" : "뜻: " + h.getMeaning());
        reading.getStyleClass().add("title");
        meaning.getStyleClass().add("title");
        Label tip = new Label("다시 클릭하면 앞면으로 돌아갑니다");
        tip.getStyleClass().add("counter");
        box.getChildren().addAll(ch, reading, meaning, tip);
        return box;
    }

    private void flipCard() {
        if (cardList.isEmpty()) return;
        ScaleTransition out = new ScaleTransition(Duration.millis(140), cardStack);
        out.setFromX(1); out.setToX(0);
        out.setOnFinished(e -> {
            showingBack = !showingBack;
            frontNode.setVisible(!showingBack);
            backNode.setVisible(showingBack);
            ScaleTransition in = new ScaleTransition(Duration.millis(140), cardStack);
            in.setFromX(0); in.setToX(1);
            in.play();
        });
        out.play();
    }

    private void saveCardState() {
        String last = (cardList.isEmpty() ? null : cardList.get(cardIndex).getCharacter());
        cardRepo.save(new CardState(last, Collections.emptySet()));
    }

    private int indexOfCharacter(List<Hanja> list, String ch) {
        for (int i = 0; i < list.size(); i++) if (list.get(i).getCharacter().equals(ch)) return i;
        return -1;
    }

    private void handleGlobalKeys(KeyEvent e) {
        boolean onCards = (cardsRoot != null) && !contentRoot.getChildren().isEmpty() && contentRoot.getChildren().get(0) == cardsRoot;
        if (onCards) {
            switch (e.getCode()) {
                case RIGHT -> nextBtn.fire();
                case LEFT  -> prevBtn.fire();
                case SPACE -> flipCard();
                case ESCAPE-> { saveCardState(); show(Screen.HOME); }
                default    -> {}
            }
        }
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
    private static int safeInt(Object v, int def) { return (v instanceof Integer i) ? i : def; }

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

    private void slide(Node node) {
        TranslateTransition t = new TranslateTransition(Duration.millis(200), node);
        t.setFromX(24);
        t.setToX(0);
        t.play();
    }

    public static void main(String[] args) { launch(args); }
}