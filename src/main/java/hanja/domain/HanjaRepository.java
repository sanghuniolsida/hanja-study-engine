package hanja.domain;

import java.util.List;

public interface HanjaRepository {
    List<Hanja> findAll();
}
