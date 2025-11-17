package hanja.util;

import java.io.PrintStream;
import java.util.Optional;
import java.util.concurrent.TimeUnit;


public final class TimedPrompt {
    private final PrintStream out;
    private final int wipeWidth;

    public TimedPrompt(PrintStream out) { this(out, 100); }

    public TimedPrompt(PrintStream out, int wipeWidth) {
        this.out = out;
        this.wipeWidth = Math.max(40, wipeWidth);
    }

    public Optional<String> readLineWithCountdown(AsyncLineReader in, long timeoutSec, String tail) {
        long remain = timeoutSec;
        while (remain > 0) {
            draw(remain, tail);
            Optional<String> line = in.readLine(1, TimeUnit.SECONDS);
            if (line.isPresent()) {
                clearLine();
                return line;
            }
            remain--;
        }
        clearLine();
        return Optional.empty();
    }

    private void draw(long remain, String tail) {
        String line = "⏳ 남은 시간: " + String.format("%2d", remain) + "초  " + tail;
        out.print("\r" + line);
        int pad = Math.max(0, wipeWidth - line.length());
        if (pad > 0) out.print(" ".repeat(pad));
        out.flush();
    }

    private void clearLine() {
        out.print("\r" + " ".repeat(wipeWidth) + "\r");
        out.flush();
    }
}