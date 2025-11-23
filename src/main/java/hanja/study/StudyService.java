package hanja.study;

import hanja.domain.Hanja;
import hanja.domain.HanjaRepository;
import hanja.quiz.Quiz;
import hanja.quiz.QuizStrategy;
import hanja.storage.ProgressRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class StudyService {

    private final HanjaRepository hanjaRepository;
    private final ProgressRepository progressRepository;

    private UserProgress userProgress;
    private List<Hanja> pool;

    public StudyService(HanjaRepository hanjaRepository, ProgressRepository progressRepository) {
        this.hanjaRepository = hanjaRepository;
        this.progressRepository = progressRepository;
    }

    public void startSession() {
        this.pool = hanjaRepository.findAll();
        this.userProgress = new UserProgress();
        this.userProgress.replaceAll(progressRepository.load());
    }

    public List<Quiz> generateQuizzes(QuizStrategy strategy, int count) {
        if (pool == null) throw new IllegalStateException("startSession() 먼저 호출해야 합니다");
        return strategy.selectQuizzes(pool, count);
    }

    public void record(Quiz quiz, boolean correct, LocalDate when) {
        if (userProgress == null) throw new IllegalStateException("startSession() 먼저 호출해야 합니다");
        userProgress.recordResult(quiz.hanja(), correct, when);
    }

    public void endSession() {
        if (userProgress == null) return;
        Map<String, LearningRecord> snap = userProgress.snapshot();
        progressRepository.save(snap);
    }

    public UserProgress userProgress() { return userProgress; }
}