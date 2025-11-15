package hanja.storage;

import hanja.study.LearningRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class FileProgressRepositoryTest {

    @TempDir Path temp;

    @Test
    void 저장후_다시_읽으면_동일한_진행도가_복원된다() {
        Path file = temp.resolve("progress.json");
        var repo = new FileProgressRepository(file);

        Map<String, LearningRecord> data = new LinkedHashMap<>();
        var a = new LearningRecord("人", 2, 1, LocalDate.of(2025, 11, 15), true);
        var b = new LearningRecord("力", 1, 0, LocalDate.of(2025, 11, 15), false);
        data.put("人", a);
        data.put("力", b);

        repo.save(data);
        Map<String, LearningRecord> loaded = repo.load();

        assertThat(loaded.keySet()).containsExactly("人", "力");
        assertThat(loaded.get("人").getAttempts()).isEqualTo(2);
        assertThat(loaded.get("人").getCorrect()).isEqualTo(1);
        assertThat(loaded.get("人").getLastReviewed()).isEqualTo(LocalDate.of(2025, 11, 15));
        assertThat(loaded.get("人").isLastCorrect()).isTrue();

        assertThat(loaded.get("力").getAttempts()).isEqualTo(1);
        assertThat(loaded.get("力").getCorrect()).isEqualTo(0);
        assertThat(loaded.get("力").getLastReviewed()).isEqualTo(LocalDate.of(2025, 11, 15));
        assertThat(loaded.get("力").isLastCorrect()).isFalse();
    }
}