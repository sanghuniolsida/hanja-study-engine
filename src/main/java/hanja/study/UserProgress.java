package hanja.study;

import hanja.domain.Hanja;

import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;


public class UserProgress {

    private final Map<String, LearningRecord> byChar = new LinkedHashMap<>();

    public Map<String, LearningRecord> snapshot() {
        return Collections.unmodifiableMap(byChar);
    }

    public void replaceAll(Map<String, LearningRecord> snapshot) {
        byChar.clear();
        if (snapshot != null) {
            byChar.putAll(snapshot);
        }
    }

    public void recordResult(Hanja hanja, boolean correct, LocalDate date) {
        if (hanja == null || date == null) { return; }
        String ch = hanja.getCharacter();
        LearningRecord rec = byChar.computeIfAbsent(ch, LearningRecord::new);
        rec.record(correct, date);
    }

    public boolean needsReview(Hanja hanja) {
        if (hanja == null) { return false; }
        LearningRecord rec = byChar.get(hanja.getCharacter());
        if (rec == null) { return false; }
        return rec.needsReview();
    }

    public LearningRecord getRecord(String character) {
        return byChar.get(character);
    }
}
