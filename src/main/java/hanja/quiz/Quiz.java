package hanja.quiz;

import hanja.domain.Hanja;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static hanja.util.AnswerNormalizer.normalize;

public class Quiz {
    private final Hanja hanja;
    private final QuestionType type;

    // ---- MCQ(4지선다) 전용 필드 ----
    private final boolean mcq;
    private final List<String> options;
    private final int correctIndex;
    private final String mcqPromptOverride;

    // 기존(주관식) 생성자
    public Quiz(Hanja hanja, QuestionType type) {
        this.hanja = Objects.requireNonNull(hanja, "hanja");
        this.type  = Objects.requireNonNull(type, "type");
        this.mcq = false;
        this.options = List.of();
        this.correctIndex = -1;
        this.mcqPromptOverride = null;
    }

    // MCQ 생성용 정적 팩토리
    public static Quiz mcq(Hanja hanja,
                           QuestionType type,
                           String promptOverride,
                           List<String> options,
                           int correctIndex) {
        Objects.requireNonNull(hanja, "hanja");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(options, "options");
        if (!(type == QuestionType.MCQ_TEXT_TO_HANJA || type == QuestionType.MCQ_HANJA_TO_TEXT)) {
            throw new IllegalArgumentException("MCQ type only: " + type);
        }
        if (options.size() != 4) {
            throw new IllegalArgumentException("MCQ requires exactly 4 options");
        }
        if (correctIndex < 0 || correctIndex >= options.size()) {
            throw new IllegalArgumentException("correctIndex out of range: " + correctIndex);
        }
        return new Quiz(hanja, type, true, List.copyOf(options), correctIndex, promptOverride);
    }

    private Quiz(Hanja hanja,
                 QuestionType type,
                 boolean mcq,
                 List<String> options,
                 int correctIndex,
                 String mcqPromptOverride) {
        this.hanja = hanja;
        this.type = type;
        this.mcq = mcq;
        this.options = options;
        this.correctIndex = correctIndex;
        this.mcqPromptOverride = mcqPromptOverride;
    }

    public Hanja hanja() { return hanja; }
    public QuestionType type() { return type; }

    public boolean isMcq() { return mcq; }
    public List<String> options() { return Collections.unmodifiableList(options); }
    public int correctIndex() { return correctIndex; }

    public String prompt() {
        if (mcq && mcqPromptOverride != null) return mcqPromptOverride;

        return switch (type) {
            case HANJA_TO_MEANING -> "다음 한자의 뜻은? : " + hanja.getCharacter();
            case MEANING_TO_HANJA -> "다음 뜻의 한자는? : " + hanja.getMeaning();
            case HANJA_TO_READING -> "다음 한자의 음(독음)은? : " + hanja.getCharacter();

            case MCQ_TEXT_TO_HANJA, MCQ_HANJA_TO_TEXT -> (mcqPromptOverride != null
                    ? mcqPromptOverride
                    : hanja.getCharacter());
        };
    }

    public boolean isCorrect(String answer) {
        if (answer == null) return false;

        if (mcq) {
            String trimmed = answer.trim();
            try {
                int idx = Integer.parseInt(trimmed);
                if (idx == correctIndex) return true;
            } catch (NumberFormatException ignore) {
            }
            String want = options.get(correctIndex);
            return normalize(trimmed).equals(normalize(want));
        }

        String expected = switch (type) {
            case HANJA_TO_MEANING -> hanja.getMeaning();
            case MEANING_TO_HANJA -> hanja.getCharacter();
            case HANJA_TO_READING -> hanja.getReading();
            case MCQ_TEXT_TO_HANJA, MCQ_HANJA_TO_TEXT -> "";
        };
        return normalize(answer).equals(normalize(expected));
    }
}