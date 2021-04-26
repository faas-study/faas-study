package faas.study.service;

import faas.study.entity.TrainType;
import faas.study.repository.TrainTypeRepository;
import faas.study.repository.TrainTypeRepositoryImpl;

public class TrainServiceImpl implements TrainService {

    private TrainTypeRepository repository=new TrainTypeRepositoryImpl();

    @Override
    public TrainType retrieve(String id) {
        if (repository.findById(id) == null) {
            return null;
        } else {
            return repository.findById(id);
        }
    }

}
