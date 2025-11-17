package hanja.ui.session;

import java.util.List;

public record SessionResult(List<Attempt> attempts, int correctCount, boolean exitedEarly) { }
