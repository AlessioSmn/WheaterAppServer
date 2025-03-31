package it.unipi.lsmsd.controller;

import it.unipi.lsmsd.DTO.CityDTO;
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

    @PostMapping("/add")

    public ResponseEntity<String> addToFavorites(@RequestHeader("Authorization") String token, @RequestBody String targetCity){
        try{
            favoriteCityService.addToFavorites(token, targetCity);
            // success
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body("City added to fav-list successfully");

        }catch(Exception ex){
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + ex.getMessage());
        }
    }

    @PostMapping("/remove")
    public ResponseEntity<String> removeFromFavorites(@RequestHeader("Authorization") String token, @RequestBody String targeCity){
        try{
            favoriteCityService.removeFromFavorites(token, targeCity);
            // success
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body("City removed from fav-list successfully");

        }catch(Exception ex){
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + ex.getMessage());
        }
    }
}
