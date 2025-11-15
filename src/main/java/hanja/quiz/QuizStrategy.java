package hanja.quiz;

import hanja.domain.Hanja;
import java.util.List;

public interface QuizStrategy {
    List<Quiz> selectQuizzes(List<Hanja> pool, int count);
}
