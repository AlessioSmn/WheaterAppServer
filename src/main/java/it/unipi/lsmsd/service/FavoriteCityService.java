package it.unipi.lsmsd.service;

import it.unipi.lsmsd.model.User;
import it.unipi.lsmsd.model.City;
import it.unipi.lsmsd.repository.UserRepository;
import it.unipi.lsmsd.repository.CityRepository;
import it.unipi.lsmsd.utility.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FavoriteCityService {

    @Autowired
    private UserRepository userRepository; // Dependency injection for the User repository
    @Autowired
    private CityRepository cityRepository; // Dependency injection for the City repository

    // Helper method to retrieve the User object using the provided token
    private User getUserFromToken(String token) throws RuntimeException {
        // Extract the reduced user (just username and role) from the token
        User reducedUser = JWTUtil.extractUser(token);

        // Fetch the complete user from the database using the username
        Optional<User> userOpt = userRepository.findByUsername(reducedUser.getUsername());

        // If the user is not found in the database, throw an exception
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        // Return the complete User object if found
        return userOpt.get();
    }

    // Helper method to retrieve the City object by its name
    private City getCityByName(String targetCity) throws RuntimeException {
        // Search for the city in the database by its name
        Optional<City> cityOpt = cityRepository.findByName(targetCity);

        // If the city is not found, throw an exception
        if (cityOpt.isEmpty()) {
            throw new RuntimeException("Target city not found");
        }

        // Return the City object if found
        return cityOpt.get();
    }

    // Method to add a city to the user's favorite cities list
    public String addToFavorites(String token, String targetCity) throws Exception {
        try {
            // Retrieve the user and city using the helper methods
            User user = getUserFromToken(token);
            City city = getCityByName(targetCity);

            // Get the user's current list of favorite cities
            List<City> listCity = user.getListCity();

            // Check if the city is already in the user's favorite cities list
            if (!listCity.contains(city)) {
                listCity.add(city);
                user.setListCity(listCity);
                userRepository.save(user);
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
    public String removeFromFavorites(String token, String targetCity) throws Exception {
        try {
            // Retrieve the user and city using the helper methods
            User user = getUserFromToken(token);
            City city = getCityByName(targetCity);

            // Get the user's current list of favorite cities
            List<City> listCity = user.getListCity();

            // Check if the city is in the user's favorite cities list
            if (listCity.contains(city)) {
                listCity.remove(city);
                user.setListCity(listCity);
                userRepository.save(user);
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
