package it.unipi.lsmsd.repository;

import it.unipi.lsmsd.model.City;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CityRepository extends MongoRepository<City, String> { }
