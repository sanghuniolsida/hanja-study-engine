package hanja.study;

import hanja.domain.Hanja;

import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 사용자 학습 진행도 집계.
 * - key: character(한 글자)
 * - recordResult로 누적, needsReview로 오답 우선 선별 기반 제공
 */
public class UserProgress {

    private final Map<String, LearningRecord> byChar = new LinkedHashMap<>();

    public void recordResult(Hanja hanja, boolean correct) {
        recordResult(hanja, correct, LocalDate.now());
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

    public List<LearningRecord> pendingReviews() {
        return byChar.values().stream()
                .filter(LearningRecord::needsReview)
                .toList();
    }

    public Map<String, LearningRecord> asUnmodifiableMap() {
        return Collections.unmodifiableMap(byChar);
    }
}
