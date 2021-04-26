package faas.study.service;

import faas.study.repository.StationRepository;
import faas.study.repository.StationRepositoryImpl;
import faas.study.util.Response;
import faas.study.entity.Station;

public class StationServiceImpl implements StationService {

    private StationRepository repository=new StationRepositoryImpl();

    String success = "Success";

    @Override
    public Response queryForId(String stationName) {
        Station station = repository.findByName(stationName);
        if (station  != null) {
            return new Response<>(1, success, station.getId());
        } else {
            return new Response<>(0, "Not exists", stationName);
        }
    }

}
