package hanja.storage;

import hanja.study.LearningRecord;
import java.util.Map;

public interface ProgressRepository {
    Map<String, LearningRecord> load();
    void save(Map<String, LearningRecord> data);
}
