package fudan.se.repository;
import fudan.se.entity.TrainType;

public interface TrainTypeRepository {

    TrainType findById(String id);

}
