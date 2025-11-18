package hanja.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.*;
import java.util.LinkedHashSet;

public class FileCardStateRepository implements CardStateRepository {
    private final Path path;
    private final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public FileCardStateRepository() {
        this(getDefaultPath());
    }

    public FileCardStateRepository(Path path) {
        this.path = path;
    }

    private static Path getDefaultPath() {
        String home = System.getProperty("user.home");
        Path dir = Paths.get(home, ".hanja");
        return dir.resolve("cards-state.json");
    }

    @Override
    public CardState load() {
        try {
            Path dir = path.getParent();
            if (dir != null && !Files.exists(dir)) Files.createDirectories(dir);
            if (!Files.exists(path)) return new CardState(null, new LinkedHashSet<>());
            return mapper.readValue(Files.readString(path), CardState.class);
        } catch (IOException e) {
            return new CardState(null, new LinkedHashSet<>());
        }
    }

    @Override
    public void save(CardState state) {
        try {
            Path dir = path.getParent();
            if (dir != null && !Files.exists(dir)) Files.createDirectories(dir);
            String json = mapper.writeValueAsString(state);
            Files.writeString(path, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException ignore) {
        }
    }
}
