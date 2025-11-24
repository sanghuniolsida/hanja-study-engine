package hanja.study;

import hanja.domain.Hanja;
import hanja.domain.HanjaRepository;
import hanja.quiz.MistakeFirstStrategy;
import hanja.quiz.Quiz;
import hanja.quiz.QuizStrategy;
import hanja.storage.ProgressRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

class StudyServiceTest {

    static class StubRepo implements HanjaRepository {
        private final List<Hanja> data;
        StubRepo(List<Hanja> d){ this.data = d; }
        @Override public List<Hanja> findAll() { return data; }
        @Override public List<Hanja> findByLevel(String level) { return data; }
        @Override public Optional<Hanja> findByCharacter(String character) {
            return data.stream().filter(h -> h.getCharacter().equals(character)).findFirst();
        }
    }
    static class MemProgressRepo implements ProgressRepository {
        Map<String, LearningRecord> store = new LinkedHashMap<>();
        @Override public Map<String, LearningRecord> load() { return new LinkedHashMap<>(store); }
        @Override public void save(Map<String, LearningRecord> data) { store = new LinkedHashMap<>(data); }
    }

    private static final List<Hanja> POOL = List.of(
            new Hanja("一", "일", "하나", "8급"),
            new Hanja("二", "이", "둘",  "8급"),
            new Hanja("人", "인", "사람","8급")
    );

    @Test
    void 로드된_오답을_우선_출제하고_정답으로_기록후_저장한다() {
        var mem = new MemProgressRepo();
        mem.store.put("人", new LearningRecord("人", 1, 0, LocalDate.now().minusDays(1), false));

        var svc = new StudyService(new StubRepo(POOL), mem);

        svc.startSession();
        QuizStrategy strat = new MistakeFirstStrategy(svc.userProgress(), new java.util.Random(5));
        List<Quiz> qs = svc.generateQuizzes(strat, 2);
        assertThat(qs.stream().map(q -> q.hanja().getCharacter())).contains("人");

        for (Quiz q : qs) {
            boolean ok = q.hanja().getCharacter().equals("人");
            svc.record(q, ok, LocalDate.now());
        }
        svc.endSession();

        Map<String, LearningRecord> saved = mem.load();
        assertThat(saved.get("人").isLastCorrect()).isTrue();
        assertThat(saved.get("人").getAttempts()).isGreaterThanOrEqualTo(2);
        assertThat(saved.get("人").getCorrect()).isGreaterThanOrEqualTo(1);
    }
}