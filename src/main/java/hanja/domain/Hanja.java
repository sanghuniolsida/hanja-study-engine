package hanja.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;


public class Hanja {
    private final String character;
    private final String reading;
    private final String meaning;
    private final String level;


    @JsonCreator
    public Hanja(
            @JsonProperty("character") String character,
            @JsonProperty("reading")   String reading,
            @JsonProperty("meaning")   String meaning,
            @JsonProperty("level")     String level
    ) {
        this.character = validateNotBlank(character, "한자 문자");
        this.reading   = validateNotBlank(reading,   "독음");
        this.meaning   = validateNotBlank(meaning,   "의미");
        this.level     = validateNotBlank(level,     "급수");
    }


    private String validateNotBlank(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    fieldName + "는 필수 값입니다"
            );
        }
        return value.trim();
    }

    public String getCharacter() { return character; }
    public String getReading()   { return reading; }
    public String getMeaning()   { return meaning; }
    public String getLevel()     { return level; }


    public boolean isLevel(String targetLevel) {
        return targetLevel != null && this.level.equals(targetLevel);
    }


    @Override
    public boolean equals(Object comparisonTarget) {
        if (this == comparisonTarget) return true;
        if (!(comparisonTarget instanceof Hanja)) return false;
        Hanja other = (Hanja) comparisonTarget;
        return Objects.equals(character, other.character);
    }

    @Override
    public int hashCode() {
        return Objects.hash(character);
    }

    @Override
    public String toString() {
        return String.format("Hanja{%s(%s): %s [%s]}",
                character, reading, meaning, level);
    }
}