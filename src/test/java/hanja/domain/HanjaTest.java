package hanja.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class HanjaTest {

    @Test
    void 생성_성공() {
        var h = new Hanja("學", "학", "배울 학", "3급");
        assertThat(h.getCharacter()).isEqualTo("學");
        assertThat(h.getReading()).isEqualTo("학");
        assertThat(h.getMeaning()).isEqualTo("배울 학");
        assertThat(h.getLevel()).isEqualTo("3급");
    }

    @Test
    void 공백_null이면_예외() {
        assertThatThrownBy(() -> new Hanja(" ", "학", "배울 학", "3급"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Hanja("學", null, "배울 학", "3급"))
                .isInstanceOf(IllegalArgumentException.class);
    }


    @Test
    void 동등성은_문자만_비교한다() {
        var a = new Hanja("人", "인", "사람", "8급");
        var b = new Hanja("人", "인", "사람", "7급");
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void isLevel_null이면_false() {
        var h = new Hanja("力", "력", "힘", "8급");
        assertThat(h.isLevel(null)).isFalse();
        assertThat(h.isLevel("8급")).isTrue();
    }
}