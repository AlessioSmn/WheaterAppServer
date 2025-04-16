package it.unipi.lsmsd.controller;

import it.unipi.lsmsd.exception.CityAlreadyInFavoritesException;
import it.unipi.lsmsd.exception.CityNotInFavoritesException;

import it.unipi.lsmsd.service.FavoriteCityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/favorites")
public class FavoriteCitiesController {

    @Autowired
    private FavoriteCityService favoriteCityService;

    @PostMapping("/get")
    public ResponseEntity<String> getFavorites(@RequestHeader("Authorization") String token){
        try{
            String result = favoriteCityService.getFavorites(token);
            // success
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(result);

        }
        catch(Exception ex){
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + ex.getMessage());
        }
    }

    @PostMapping("/add")
    public ResponseEntity<String> addToFavorites(@RequestHeader("Authorization") String token, @RequestBody String targetCity){
        try{
            String result = favoriteCityService.addToFavorites(token, targetCity);
            // success
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(result);

        }
        catch(CityAlreadyInFavoritesException ex){
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(ex.getMessage());
        }
        catch(Exception ex){
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + ex.getMessage());
        }
    }

    @PostMapping("/remove")
    public ResponseEntity<String> removeFromFavorites(@RequestHeader("Authorization") String token, @RequestBody String targeCity){
        try{
            String result = favoriteCityService.removeFromFavorites(token, targeCity);
            // success
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(result);
        }
        catch(CityNotInFavoritesException ex){
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(ex.getMessage());
        }
        catch(Exception ex){
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + ex.getMessage());
        }
    }
}
