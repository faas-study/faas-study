package fudan.se.repository;

import fudan.se.entity.Assurance;

import java.util.UUID;


public interface AssuranceRepository {

    /**
     * find by order id
     *
     * @param orderId order id
     * @return Assurance
     */
    Assurance findByOrderId(UUID orderId);

    void save(Assurance assurance);

}
