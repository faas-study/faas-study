package fudan.se.repository;

import fudan.se.entity.ConsignPrice;


public interface ConsignPriceConfigRepository{

    /**
     * find by index
     *
     * @param index index
     * @return ConsignPrice
     */
    ConsignPrice findByIndex(int index);

}
