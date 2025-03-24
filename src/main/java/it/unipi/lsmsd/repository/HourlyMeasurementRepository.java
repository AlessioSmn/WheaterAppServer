package it.unipi.lsmsd.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import it.unipi.lsmsd.model.HourlyMeasurement;

import java.util.List;

public interface HourlyMeasurementRepository extends MongoRepository<HourlyMeasurement, String> {
    List<HourlyMeasurement> findByCityId(String cityId);
}

