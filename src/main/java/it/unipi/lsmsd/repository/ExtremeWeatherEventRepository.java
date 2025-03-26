package it.unipi.lsmsd.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import it.unipi.lsmsd.model.ExtremeWeatherEvent;
import java.util.Optional;

public interface ExtremeWeatherEventRepository extends MongoRepository<ExtremeWeatherEvent, String> {

}
