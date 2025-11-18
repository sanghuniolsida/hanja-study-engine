package hanja.storage;

import java.util.LinkedHashSet;
import java.util.Set;

public record CardState(String lastCharacter, Set<String> favorites) {
    public CardState {
        favorites = (favorites == null) ? new LinkedHashSet<>() : new LinkedHashSet<>(favorites);
    }
}