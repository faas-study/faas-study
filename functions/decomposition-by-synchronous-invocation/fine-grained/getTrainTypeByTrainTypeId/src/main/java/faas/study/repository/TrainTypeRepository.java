package faas.study.repository;
import faas.study.entity.TrainType;

public interface TrainTypeRepository {

    TrainType findById(String id);

}
