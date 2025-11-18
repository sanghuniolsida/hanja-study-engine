package hanja.storage;

public interface CardStateRepository {
    CardState load();
    void save(CardState state);
}