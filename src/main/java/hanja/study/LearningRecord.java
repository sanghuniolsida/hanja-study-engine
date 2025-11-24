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
        this(character, 0, 0, null, false);
    }

    public LearningRecord(String character, int attempts, int correct,
                          LocalDate lastReviewed, boolean lastCorrect) {
        this.character = Objects.requireNonNull(character, "character");
        this.attempts = Math.max(0, attempts);
        this.correct = Math.max(0, Math.min(this.attempts, correct));
        this.lastReviewed = lastReviewed;
        this.lastCorrect = (this.attempts > 0) && lastCorrect;
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

    public double accuracy() {
        if (attempts == 0) { return 0.0; }
        return (double) correct / (double) attempts;
    }

    public boolean needsReview() {
        return !lastCorrect && attempts > 0;
    }
}
