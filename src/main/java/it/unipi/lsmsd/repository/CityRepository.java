package it.unipi.lsmsd.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import it.unipi.lsmsd.model.City;
import java.util.Optional;

public interface CityRepository extends MongoRepository<City, String> {
    Optional<City> findByName(String name);
}