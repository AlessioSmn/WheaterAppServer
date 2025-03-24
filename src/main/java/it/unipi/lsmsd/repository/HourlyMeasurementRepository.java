package it.unipi.lsmsd.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import it.unipi.lsmsd.model.HourlyMeasurement;

public interface HourlyMeasurementRepository extends MongoRepository<HourlyMeasurement, String> { 
}

