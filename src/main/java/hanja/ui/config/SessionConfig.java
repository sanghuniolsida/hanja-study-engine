package hanja.ui.config;

import java.util.Collections;
import java.util.List;

public final class SessionConfig {

    public enum Mode { MEANING, READING, MIX }

    private final List<String> levels; // 비어있으면 전체
    private final int count;           // 출제 문항 수
    private final long timeoutSec;     // 문제당 제한 시간
    private final Mode mode;           // 뜻/음/섞기
    private final Long seed;           // 재현용 시드(옵션)

    public SessionConfig(List<String> levels, int count, long timeoutSec, Mode mode, Long seed) {
        this.levels = levels == null ? List.of() : List.copyOf(levels);
        this.count = Math.max(1, count);
        this.timeoutSec = Math.max(1, timeoutSec);
        this.mode = mode == null ? Mode.MEANING : mode;
        this.seed = seed;
    }

    public List<String> levels() { return Collections.unmodifiableList(levels); }
    public int count() { return count; }
    public long timeoutSec() { return timeoutSec; }
    public Mode mode() { return mode; }
    public Long seed() { return seed; }

    public static SessionConfig defaultConfig() {
        return new SessionConfig(List.of(), 20, 30, Mode.MEANING, null);
    }
}