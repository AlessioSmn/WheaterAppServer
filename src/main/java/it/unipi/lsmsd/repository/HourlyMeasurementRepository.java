package it.unipi.lsmsd.repository;

import it.unipi.lsmsd.model.HourlyMeasurement;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;

@Repository
public interface HourlyMeasurementRepository extends MongoRepository<HourlyMeasurement, String> {
    List<HourlyMeasurement> findByCityIdAndTimeBetween(String cityId, Date startTime, Date endTime);
}

