package it.unipi.lsmsd.repository;

import it.unipi.lsmsd.model.City;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.MongoRepository;

@Repository
public interface CityRepository extends MongoRepository<City, String> { }
