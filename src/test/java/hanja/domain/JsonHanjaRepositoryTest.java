package hanja.domain;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

class JsonHanjaRepositoryTest {

    @Test
    void 리소스를_로드하면_목록을_반환한다() {
        var repo = new JsonHanjaRepository();
        List<Hanja> list = repo.findAll();
        assertThat(list).isNotEmpty();
        assertThat(list.get(0).getCharacter()).isNotBlank();
    }

    @Test
    void 없는_경로면_예외() {
        var repo = new JsonHanjaRepository("/no-such.json");
        assertThatThrownBy(repo::findAll).isInstanceOf(RuntimeException.class);
    }
}
