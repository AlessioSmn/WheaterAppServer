package it.unipi.lsmsd.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import it.unipi.lsmsd.model.ExtremeWeatherEvent;

import java.util.List;
import java.util.Optional;

public interface ExtremeWeatherEventRepository extends MongoRepository<ExtremeWeatherEvent, String> {
    List<ExtremeWeatherEvent> findByLongitudeAndLatitudeAndRadiusAndDateEndIsNull(Double longitude, Double latitude, Integer radius);
}
