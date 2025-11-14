package hanja.domain;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.List;

public class JsonHanjaRepository implements HanjaRepository {
    private static final String DEFAULT_PATH = "/hanja-data.json";
    private final ObjectMapper mapper = new ObjectMapper();
    private final String resourcePath;

    public JsonHanjaRepository() { this(DEFAULT_PATH); }
    public JsonHanjaRepository(String resourcePath) { this.resourcePath = resourcePath; }

    @Override
    public List<Hanja> findAll() {
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) throw new IllegalStateException("리소스를 찾을 수 없습니다: " + resourcePath);
            return mapper.readValue(is, new TypeReference<List<Hanja>>() {});
        } catch (Exception e) {
            throw new RuntimeException("한자 데이터를 읽는 중 오류 발생", e);
        }
    }
}
