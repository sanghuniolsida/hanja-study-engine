package hanja.ui.session;

import hanja.quiz.Quiz;

/** 한 문제 시도 결과 */
public record Attempt(Quiz quiz, String userAnswer, boolean correct) {
    public static final String TIMEOUT = "<TIMEOUT>";
}
