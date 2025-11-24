package hanja.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hanja.study.LearningRecord;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;


public class FileProgressRepository implements ProgressRepository {

    private static final String DEFAULT_FILE = "progress.json";

    private final Path path;
    private final ObjectMapper om = new ObjectMapper();

    public FileProgressRepository() {
        this(Path.of(DEFAULT_FILE));
    }

    public FileProgressRepository(Path path) {
        this.path = path;
    }

    @Override
    public Map<String, LearningRecord> load() {
        if (!Files.exists(path)) return new LinkedHashMap<>();
        try {
            byte[] json = Files.readAllBytes(path);
            Map<String, Persisted> raw = om.readValue(json, new TypeReference<Map<String, Persisted>>() {});
            Map<String, LearningRecord> out = new LinkedHashMap<>();
            for (var e : raw.entrySet()) {
                String ch = e.getKey();
                Persisted p = e.getValue();
                LocalDate date = (p.lastReviewedEpochDay > 0) ? LocalDate.ofEpochDay(p.lastReviewedEpochDay) : null;
                out.put(ch, new LearningRecord(ch, p.attempts, p.correct, date, p.lastCorrect));
            }
            return out;
        } catch (IOException e) {
            throw new RuntimeException("진행도 로딩 실패: " + path, e);
        }
    }

    @Override
    public void save(Map<String, LearningRecord> data) {
        try {
            Map<String, Persisted> raw = new LinkedHashMap<>();
            for (var e : data.entrySet()) {
                String ch = e.getKey();
                LearningRecord r = e.getValue();
                Persisted p = new Persisted();
                p.attempts = r.getAttempts();
                p.correct = r.getCorrect();
                p.lastReviewedEpochDay = (r.getLastReviewed() == null) ? 0L : r.getLastReviewed().toEpochDay();
                p.lastCorrect = r.isLastCorrect();
                raw.put(ch, p);
            }
            byte[] json = om.writerWithDefaultPrettyPrinter().writeValueAsBytes(raw);
            Files.writeString(path, new String(json));
        } catch (IOException e) {
            throw new RuntimeException("진행도 저장 실패: " + path, e);
        }
    }

    /** JSON 직렬화 전용 DTO */
    public static final class Persisted {
        public int attempts;
        public int correct;
        public long lastReviewedEpochDay;
        public boolean lastCorrect;

        public Persisted() {}
    }
}