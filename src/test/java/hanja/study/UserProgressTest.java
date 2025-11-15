package hanja.study;

import hanja.domain.Hanja;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

class UserProgressTest {

    private static final Hanja 人 = new Hanja("人", "인", "사람", "8급");
    private static final Hanja 力 = new Hanja("力", "력", "힘", "8급");

    @Test
    void 오답이면_리뷰대상_true() {
        var up = new UserProgress();
        up.recordResult(人, false, LocalDate.of(2025, 11, 15));

        assertThat(up.needsReview(人)).isTrue();
        assertThat(up.needsReview(力)).isFalse(); // 기록 없으면 false
    }

    @Test
    void 정답으로_갱신하면_리뷰대상_false() {
        var up = new UserProgress();
        up.recordResult(人, false, LocalDate.of(2025, 11, 14)); // 오답
        up.recordResult(人, true,  LocalDate.of(2025, 11, 15)); // 정답

        assertThat(up.needsReview(人)).isFalse();
    }

    @Test
    void 시도수_정답수_정확히_누적된다() {
        var up = new UserProgress();
        up.recordResult(人, false, LocalDate.of(2025, 11, 15));
        up.recordResult(人, true,  LocalDate.of(2025, 11, 15));

        var rec = up.getRecord("人");
        assertThat(rec.getAttempts()).isEqualTo(2);
        assertThat(rec.getCorrect()).isEqualTo(1);
        assertThat(rec.accuracy()).isEqualTo(0.5);
    }

    @Test
    void 최종_복습일이_최근값으로_갱신된다() {
        var up = new UserProgress();
        up.recordResult(人, false, LocalDate.of(2025, 11, 14));
        up.recordResult(人, true,  LocalDate.of(2025, 11, 15));

        var rec = up.getRecord("人");
        assertThat(rec.getLastReviewed()).isEqualTo(LocalDate.of(2025, 11, 15));
    }
}