package hanja.study;

import java.time.LocalDate;
import java.util.Objects;

public class LearningRecord {
    private final String character;
    private int attempts;
    private int correct;
    private LocalDate lastReviewed;
    private boolean lastCorrect;

    public LearningRecord(String character) {
        this.character = Objects.requireNonNull(character, "character");
        this.attempts = 0;
        this.correct = 0;
        this.lastReviewed = null;
        this.lastCorrect = false;
    }

    public void record(boolean ok, LocalDate date) {
        attempts += 1;
        if (ok) { correct += 1; }
        lastReviewed = date;
        lastCorrect = ok;
    }

    public String getCharacter() { return character; }
    public int getAttempts() { return attempts; }
    public int getCorrect() { return correct; }
    public LocalDate getLastReviewed() { return lastReviewed; }
    public boolean isLastCorrect() { return lastCorrect; }

    /** 0.0 ~ 1.0 */
    public double accuracy() {
        if (attempts == 0) { return 0.0; }
        return (double) correct / (double) attempts;
    }

    /** “어제 틀린 건 오늘 다시” 룰의 최소 구현: 최근 결과가 오답이면 복습 대상 */
    public boolean needsReview() {
        return !lastCorrect && attempts > 0;
    }
}
