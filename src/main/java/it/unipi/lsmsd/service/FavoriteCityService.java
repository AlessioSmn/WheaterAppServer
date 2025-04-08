package it.unipi.lsmsd.service;

import it.unipi.lsmsd.model.Role;
import it.unipi.lsmsd.model.User;
import it.unipi.lsmsd.model.City;
import it.unipi.lsmsd.repository.UserRepository;
import it.unipi.lsmsd.repository.CityRepository;
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
    @Autowired
    private UserService userService;

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
            User user = userService.getAndCheckUserFromToken(token, Role.USER);

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
            User user = userService.getUserFromToken(token);
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
