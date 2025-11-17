package hanja.ui;

import hanja.ui.config.SessionConfig;
import hanja.ui.config.SessionConfig.Mode;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;


public final class ConsolePrompter {

    public SessionConfig prompt() {
        Scanner sc = new Scanner(System.in, StandardCharsets.UTF_8);

        System.out.print("학습 급수 입력 (예: 8급 또는 7급,8급, 비우면 전체): ");
        String lvRaw = sc.hasNextLine() ? sc.nextLine().trim() : "";
        List<String> levels = lvRaw.isEmpty()
                ? List.of()
                : Arrays.stream(lvRaw.split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());

        int count = 20;
        System.out.print("문항 수 입력 (기본 20): ");
        if (sc.hasNextLine()) {
            String nRaw = sc.nextLine().trim();
            if (!nRaw.isEmpty()) {
                try { count = Math.max(1, Integer.parseInt(nRaw)); } catch (NumberFormatException ignore) {}
            }
        }

        Mode mode = Mode.MEANING;
        System.out.print("모드 선택 [1] 뜻  [2] 음(독음)  [3] 섞어서  (기본 1): ");
        if (sc.hasNextLine()) {
            String mRaw = sc.nextLine().trim();
            if ("2".equals(mRaw)) mode = Mode.READING;
            else if ("3".equals(mRaw)) mode = Mode.MIX;
        }

        long timeoutSec = 30;
        System.out.print("문제당 제한 시간(초) 입력 (기본 30): ");
        if (sc.hasNextLine()) {
            String tRaw = sc.nextLine().trim();
            if (!tRaw.isEmpty()) {
                try { timeoutSec = Math.max(1, Long.parseLong(tRaw)); } catch (NumberFormatException ignore) {}
            }
        }

        return new SessionConfig(levels, count, timeoutSec, mode, null);
    }
}