package it.unipi.lsmsd.repository;

import it.unipi.lsmsd.model.City;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

@Repository
public interface CityRepository extends MongoRepository<City, String> {
    List<City> findAllByName(String name);
    List<City> findByRegion(String region);
}
