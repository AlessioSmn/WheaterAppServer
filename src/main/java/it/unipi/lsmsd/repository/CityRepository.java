package it.unipi.lsmsd.repository;

import it.unipi.lsmsd.model.City;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CityRepository extends MongoRepository<City, String> {
    Optional<City> findByName(String name);
    List<City> findByRegion(String region);
}
