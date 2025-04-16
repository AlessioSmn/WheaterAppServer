package it.unipi.lsmsd.service;

import it.unipi.lsmsd.model.Role;
import it.unipi.lsmsd.model.User;
import it.unipi.lsmsd.model.City;
import it.unipi.lsmsd.repository.UserRepository;
import it.unipi.lsmsd.repository.CityRepository;
import it.unipi.lsmsd.exception.CityAlreadyInFavoritesException;
import it.unipi.lsmsd.exception.CityNotInFavoritesException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
            throw new RuntimeException("Target city not found: " + targetCity);
        }

        // Return the City object if found
        return cityOpt.get();
    }

    // Method to get user's favorite cities list
    public String getFavorites(String token){
        User user = userService.getAndCheckUserFromToken(token, Role.USER);
        List<City> listCity = user.getListCity();

        if(listCity.isEmpty()){
            return "Favorite cities list is empty";
        }

        return listCity.stream()
                .map(city -> city.getName() + " (" + city.getRegion() + ")")
                .collect(Collectors.joining(", "));
    }

    // Method to add a city to the user's favorite cities list
    public String addToFavorites(String token, String targetCity) throws Exception {
        User user = userService.getAndCheckUserFromToken(token, Role.USER);
        City city = getCityByName(targetCity);

        List<City> listCity = user.getListCity();

        if (listCity.contains(city)) {
            throw new CityAlreadyInFavoritesException("City is already in your favorites.");
        }

        listCity.add(city);
        user.setListCity(listCity);
        userRepository.save(user);
        return "City added to favorites successfully!";
    }

    public String removeFromFavorites(String token, String targetCity) throws Exception {
        User user = userService.getUserFromToken(token);
        City city = getCityByName(targetCity);

        List<City> listCity = user.getListCity();

        if (!listCity.contains(city)) {
            throw new CityNotInFavoritesException("City is not in your favorites.");
        }

        listCity.remove(city);
        user.setListCity(listCity);
        userRepository.save(user);
        return "City removed from favorites successfully!";
    }
}
