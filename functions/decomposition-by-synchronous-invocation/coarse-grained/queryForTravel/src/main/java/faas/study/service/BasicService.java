package faas.study.service;

import faas.study.entity.Travel;
import faas.study.util.Response;

public interface BasicService {

    /**
     * query for travel with travel information
     *
     * @param info information
     * @return Response
     */
    Response queryForTravel(Travel info);

}
