package hanja.ui;

import hanja.domain.Hanja;
import hanja.domain.JsonHanjaRepository;
import hanja.ui.config.SessionConfig;
import hanja.ui.session.SessionResult;
import hanja.ui.session.SessionRunner;
import hanja.ui.view.ReviewPrinter;

import java.io.FileDescriptor;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ConsoleApp {

    public static void main(String[] args) {
        forceUtf8Console();

        SessionConfig cfg = new ConsolePrompter().prompt();

        var repo = new JsonHanjaRepository();
        List<Hanja> all = repo.findAll();

        SessionResult result = new SessionRunner().run(all, cfg);

        new ReviewPrinter().print(result.attempts());
        String suffix = result.exitedEarly() ? " (중도 종료)" : "";
        System.out.println("결과: " + result.correctCount() + " / " + result.attempts().size() + suffix);
    }

    private static void forceUtf8Console() {
        System.setOut(new PrintStream(new java.io.FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(new java.io.FileOutputStream(FileDescriptor.err), true, StandardCharsets.UTF_8));
    }
}