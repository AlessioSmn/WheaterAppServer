package it.unipi.lsmsd.repository;

<<<<<<< HEAD
import it.unipi.lsmsd.model.City;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

@Repository
public interface CityRepository extends MongoRepository<City, String> {
    Optional<City> findByName(String name);
}
=======
import org.springframework.data.mongodb.repository.MongoRepository;
import it.unipi.lsmsd.model.City;
import java.util.Optional;

public interface CityRepository extends MongoRepository<City, String> {
    Optional<City> findByName(String name);
}
>>>>>>> 0fec31d (add favorite-cities functionalities)
