package hanja.util;

import java.text.Normalizer;

public final class AnswerNormalizer {
    private AnswerNormalizer() {}

    /**
     * 채점용 문자열 정규화:
     * - 앞뒤 공백 제거, 내부 연속 공백 1칸
     * - 양끝 괄호/따옴표 제거
     */
    public static String normalize(String s) {
        if (s == null) return "";
        String t = Normalizer.normalize(s, Normalizer.Form.NFKC);
        t = t.trim().replaceAll("\\s+", " ");
        t = t.replaceAll("^[\"'()\\[\\]{}]+|[\"'()\\[\\]{}]+$", "");
        t = t.toLowerCase();
        return t;
    }
}