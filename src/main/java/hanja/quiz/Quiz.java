package hanja.quiz;

import hanja.domain.Hanja;
import java.util.Objects;

import static hanja.util.AnswerNormalizer.normalize;

public class Quiz {
    private final Hanja hanja;
    private final QuestionType type;

    public Quiz(Hanja hanja, QuestionType type) {
        this.hanja = Objects.requireNonNull(hanja, "hanja");
        this.type  = Objects.requireNonNull(type, "type");
    }

    public Hanja hanja() { return hanja; }
    public QuestionType type() { return type; }

    public String prompt() {
        return switch (type) {
            case HANJA_TO_MEANING -> "다음 한자의 뜻은? : " + hanja.getCharacter();
            case MEANING_TO_HANJA -> "다음 뜻의 한자는? : " + hanja.getMeaning();
            case HANJA_TO_READING -> "다음 한자의 음(독음)은? : " + hanja.getCharacter();
        };
    }

    public boolean isCorrect(String answer) {
        if (answer == null) return false;
        String expected = switch (type) {
            case HANJA_TO_MEANING -> hanja.getMeaning();
            case MEANING_TO_HANJA -> hanja.getCharacter();
            case HANJA_TO_READING -> hanja.getReading();
        };
        return normalize(answer).equals(normalize(expected));
    }
}