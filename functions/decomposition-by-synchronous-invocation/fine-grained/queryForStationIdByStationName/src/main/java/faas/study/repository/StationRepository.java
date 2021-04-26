package faas.study.repository;

import faas.study.entity.Station;

public interface StationRepository{

    Station findByName(String name);

}
