package hanja.util;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 호출자는 poll(timeout)로 타임아웃 입력을 받을 수 있다.
 */
public final class AsyncLineReader implements AutoCloseable {
    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final Thread readerThread;

    public AsyncLineReader(InputStream in, Charset cs) {
        final BufferedReader br = new BufferedReader(new InputStreamReader(in, cs));
        this.readerThread = new Thread(() -> {
            try {
                String line;
                while (running.get() && (line = br.readLine()) != null) {
                    queue.put(line);
                }
            } catch (InterruptedException ignore) {
                Thread.currentThread().interrupt();
            } catch (IOException ignore) {
            }
        }, "stdin-reader");
        this.readerThread.setDaemon(true);
        this.readerThread.start();
    }

    public Optional<String> readLine(long timeout, TimeUnit unit) {
        try {
            String line = queue.poll(timeout, unit);
            return Optional.ofNullable(line);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }

    @Override
    public void close() {
        running.set(false);
        readerThread.interrupt();
    }
}
