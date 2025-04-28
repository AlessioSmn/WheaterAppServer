package it.unipi.lsmsd.service;

import it.unipi.lsmsd.model.User;
import it.unipi.lsmsd.model.City;
import it.unipi.lsmsd.repository.UserRepository;
import it.unipi.lsmsd.repository.CityRepository;
import it.unipi.lsmsd.utility.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class FavoriteCityService {

    @Autowired
    private UserRepository userRepository; // Dependency injection for the User repository
    @Autowired
    private CityRepository cityRepository; // Dependency injection for the City repository

    // Helper method to retrieve the User object using the provided token
    private User getUserFromToken(String token) throws NoSuchElementException {
        // Extract the reduced user (just username and role) from the token
        User reducedUser = JWTUtil.extractUser(token);

        // Fetch the complete user from the database using the username
        Optional<User> userOpt = userRepository.findByUsername(reducedUser.getUsername());

        // If the user is not found in the database, throw an exception
        if (userOpt.isEmpty()) {
            throw new NoSuchElementException("User not found");
        }

        // Return the complete User object if found
        return userOpt.get();
    }

    // Method to add a city to the user's favorite cities list
    // Transcational since we are updating two DB classes User and City
    @Transactional
    public String addToFavorites(String token, String targetCityId){
        try {
            // Retrieve the user and city
            User user = getUserFromToken(token);
            Optional<City> city_Optional = cityRepository.findById(targetCityId);
            // throws NoSuchElementException is no city is found
            if (!city_Optional.isPresent()) { throw new NoSuchElementException("City not found with id: " + targetCityId); }
            
            // Get the user's current list of favorite cities
            List<String> listCityId = user.getListCityId();


            // Check if the city is already in the user's favorite cities list
            if (!listCityId.contains(targetCityId)) {
                // Add the cityId and save the updated user
                user.getListCityId().add(targetCityId);
                userRepository.save(user);
                // Update the follower count in the city and save the updated city
                City city = city_Optional.get();
                city.setFollowers(city.getFollowers() + 1);
                cityRepository.save(city);
                return "City added to favorites successfully!";
            } else {
                return "City is already in your favorites.";
            }

        } catch (Exception ex) {
            // TODO: exception
            throw ex;
        }
    }

    // Method to remove a city from the user's favorite cities list
    // Transcational since we are updating two DB classes User and City
    @Transactional
    public String removeFromFavorites(String token, String targetCityId) {
        try {
            // Retrieve the user and city using the helper methods
            User user = getUserFromToken(token);
            Optional<City> city_Optional = cityRepository.findById(targetCityId);
            // throws NoSuchElementException is no city is found
            if (!city_Optional.isPresent()) { throw new NoSuchElementException("City not found with id: " + targetCityId); }

             // Get the user's current list of favorite cities
             List<String> listCityId = user.getListCityId();

           // Check if the city is already in the user's favorite cities list
           if (listCityId.contains(targetCityId)) {
                // Add the cityId and save the updated user
                user.getListCityId().remove(targetCityId);
                userRepository.save(user);
                // Update the follower count in the city and save the updated city
                City city = city_Optional.get();
                city.setFollowers(city.getFollowers() - 1);
                cityRepository.save(city);
                return "City removed from favorites successfully!";
            } else {
                return "City is not in your favorites.";
            }

        } catch (Exception ex) {
            // TODO: exception
            throw ex;
        }
    }
}
