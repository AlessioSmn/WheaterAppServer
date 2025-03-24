package it.unipi.lsmsd.repository;

import it.unipi.lsmsd.model.HourlyMeasurement;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.MongoRepository;

@Repository
public interface HourlyMeasurementRepository extends MongoRepository<HourlyMeasurement, String> { 
}

