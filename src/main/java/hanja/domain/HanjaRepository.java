package hanja.domain;

import java.util.List;
import java.util.Optional;

public interface HanjaRepository {
    List<Hanja> findAll();

    default List<Hanja> findByLevel(String level) {
        if (level == null || level.trim().isEmpty()) return List.of();
        return findAll().stream().filter(h -> h.isLevel(level.trim())).toList();
    }

    default Optional<Hanja> findByCharacter(String character) {
        if (character == null || character.trim().isEmpty()) return Optional.empty();
        String ch = character.trim();
        return findAll().stream().filter(h -> h.getCharacter().equals(ch)).findFirst();
    }
}
