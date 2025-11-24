package hanja.quiz;

public enum QuestionType {
    HANJA_TO_MEANING,  // "人 -> 뜻은?"
    MEANING_TO_HANJA,  // "뜻 -> 한자는?"
    HANJA_TO_READING,  // "人 -> 음은?"

    MCQ_TEXT_TO_HANJA, // 뜻·음 → 한자 고르기
    MCQ_HANJA_TO_TEXT// 한자 → (음 · 뜻) 고르기
}