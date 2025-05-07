package it.unipi.lsmsd.controller;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Accumulators.*;
import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Sorts.*;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import it.unipi.lsmsd.model.ExtremeWeatherEventCategory;
import it.unipi.lsmsd.service.AnalyticsService;
import it.unipi.lsmsd.utility.CityUtility;
import it.unipi.lsmsd.utility.Mapper;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/num-measurements-for-city-in-range")
    public ResponseEntity<Object> getMeasurementCounts(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end
    ) {
        List<Document> serviceResponse = analyticsService.getMeasurementCountByCityInRange(start, end);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(serviceResponse);
    }

    @GetMapping("/cities-most-affected-by-ewe")
    public ResponseEntity<Object> topCitiesMostAffectedByEwe(
            @RequestParam ExtremeWeatherEventCategory extremeWeatherEventCategory,
            @RequestParam int numCities
    ) {
        List<Document> serviceResponse = analyticsService.topCitiesMostAffectedByEwe(numCities, extremeWeatherEventCategory);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(serviceResponse);
    }

    @GetMapping("/cities-most-affected-by-ewe-in-time-range")
    public ResponseEntity<Object> topCitiesMostAffectedByEweInTimeRange(
            @RequestParam ExtremeWeatherEventCategory extremeWeatherEventCategory,
            @RequestParam int numCities,
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end
    ) {
        List<Document> serviceResponse = analyticsService.topCitiesMostAffectedByEweInTimeRange(numCities, extremeWeatherEventCategory, start, end);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(serviceResponse);
    }


    /*
     * STATISTICS FOR SINGLE CITIES
     */

    @GetMapping("/average-rainfall-of-city")
    public ResponseEntity<Object> averageRainfallInCityDuringPeriod(
            @RequestParam String cityId,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate
    ) {
        List<Document> response = analyticsService.averageRainfallInCityDuringPeriod(cityId, startDate, endDate);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/average-snowfall-of-city")
    public ResponseEntity<Object> averageSnowfallInCityDuringPeriod(
            @RequestParam String cityId,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate
    ) {
        List<Document> response = analyticsService.averageSnowfallInCityDuringPeriod(cityId, startDate, endDate);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/average-temperature-of-city")
    public ResponseEntity<Object> averageTemperatureInCityDuringPeriod(
            @RequestParam String cityId,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate
    ) {
        List<Document> response = analyticsService.averageTemperatureInCityDuringPeriod(cityId, startDate, endDate);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/max-temperature-of-city")
    public ResponseEntity<Object> maxTemperatureInCityDuringPeriod(
            @RequestParam String cityId,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate
    ) {
        List<Document> response = analyticsService.maxTemperatureInCityDuringPeriod(cityId, startDate, endDate);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/min-temperature-of-city")
    public ResponseEntity<Object> minTemperatureInCityDuringPeriod(
            @RequestParam String cityId,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate
    ) {
        List<Document> response = analyticsService.minTemperatureInCityDuringPeriod(cityId, startDate, endDate);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    /*
    * STATISTIC ACROSS CITIES
    */

    @GetMapping("/top-rainiest-cities")
    public ResponseEntity<Object> topXRainiestCitiesDuringPeriod(
            @RequestParam int maxNumCitiesToFind,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate
    ) {
        List<Document> response = analyticsService.topXRainiestCitiesDuringPeriod(maxNumCitiesToFind, startDate, endDate);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/top-snowiest-cities")
    public ResponseEntity<Object> topXSnowiestCitiesDuringPeriod(
            @RequestParam int maxNumCitiesToFind,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate
    ) {
        List<Document> response = analyticsService.topXSnowiestCitiesDuringPeriod(maxNumCitiesToFind, startDate, endDate);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/top-cities-with-highest-average-temperature")
    public ResponseEntity<Object> topXCitiesWithHighestAverageTemperature(
            @RequestParam int maxNumCitiesToFind,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate
    ) {
        List<Document> response = analyticsService.topXCitiesWithHighestAverageTemperature(maxNumCitiesToFind, startDate, endDate);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/top-cities-with-lowest-average-temperature")
    public ResponseEntity<Object> topXCitiesWithLowestAverageTemperature(
            @RequestParam int maxNumCitiesToFind,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate
    ) {
        List<Document> response = analyticsService.topXCitiesWithLowestAverageTemperature(maxNumCitiesToFind, startDate, endDate);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/top-cities-with-highest-recorded-temperature")
    public ResponseEntity<Object> topXCitiesWithHighestRecordedTemperature(
            @RequestParam int maxNumCitiesToFind,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate
    ) {
        List<Document> response = analyticsService.topXCitiesWithHighestRecordedTemperature(maxNumCitiesToFind, startDate, endDate);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/top-cities-with-lowest-recorded-temperature")
    public ResponseEntity<Object> topXCitiesWithLowestRecordedTemperature(
            @RequestParam int maxNumCitiesToFind,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate
    ) {
        List<Document> response = analyticsService.topXCitiesWithLowestRecordedTemperature(maxNumCitiesToFind, startDate, endDate);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    /*
     * STATISTICS FOR SINGLE REGIONS
     */

    @GetMapping("/average-temperature-in-cities-of-region")
    public ResponseEntity<Object> averageTemperatureInCitiesOfRegionDuringPeriod(
            @RequestParam String region,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate
    ) {
        List<Document> response = analyticsService.averageTemperatureInCitiesOfRegionDuringPeriod(region, startDate, endDate);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/average-temperature-of-region")
    public ResponseEntity<Object> averageTemperatureOfRegion(
            @RequestParam String region,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate
    ) {
        List<Document> response = analyticsService.averageTemperatureOfRegion(region, startDate, endDate);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/average-rainfall-in-cities-of-region")
    public ResponseEntity<Object> averageRainfallInCitiesOfRegionDuringPeriod(
            @RequestParam String region,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate
    ) {
        List<Document> response = analyticsService.averageRainfallInCitiesOfRegionDuringPeriod(region, startDate, endDate);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/average-rainfall-of-region")
    public ResponseEntity<Object> averageRainfallOfRegion(
            @RequestParam String region,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate
    ) {
        List<Document> response = analyticsService.averageRainfallOfRegion(region, startDate, endDate);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/average-snowfall-in-cities-of-region")
    public ResponseEntity<Object> averageSnowfallInCitiesOfRegionDuringPeriod(
            @RequestParam String region,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate
    ) {
        List<Document> response = analyticsService.averageSnowfallInCitiesOfRegionDuringPeriod(region, startDate, endDate);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/average-snowfall-of-region")
    public ResponseEntity<Object> averageSnowfallOfRegion(
            @RequestParam String region,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate
    ) {
        List<Document> response = analyticsService.averageSnowfallOfRegion(region, startDate, endDate);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    /*
     * STATISTICS ACROSS REGIONS
     */

    @GetMapping("/top-rainiest-regions")
    public ResponseEntity<Object> topXRainiestRegionsDuringPeriod(
            @RequestParam int maxNumRegionsToFind,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate
    ) {
        List<Document> response = analyticsService.topXRainiestRegionsDuringPeriod(maxNumRegionsToFind, startDate, endDate);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/top-snowiest-regions")
    public ResponseEntity<Object> topXSnowiestRegionsDuringPeriod(
            @RequestParam int maxNumRegionsToFind,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate
    ) {
        List<Document> response = analyticsService.topXSnowiestRegionsDuringPeriod(maxNumRegionsToFind, startDate, endDate);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/top-hottest-regions")
    public ResponseEntity<Object> topXHottestRegionsDuringPeriod(
            @RequestParam int maxNumRegionsToFind,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate
    ) {
        List<Document> response = analyticsService.topXHottestRegionsDuringPeriod(maxNumRegionsToFind, startDate, endDate);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/top-coldest-regions")
    public ResponseEntity<Object> topXColdestRegionsDuringPeriod(
            @RequestParam int maxNumRegionsToFind,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate
    ) {
        List<Document> response = analyticsService.topXColdestRegionsDuringPeriod(maxNumRegionsToFind, startDate, endDate);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }



    // Average Temperature per City for last 30 Days
    @GetMapping("/30days-avg-temperature")
    public List<Document> getAvgTemperatureLast30Days() {
        return analyticsService.getAvgTemperaturePerCityLast30Days();
    }

    // Average Temperature per City for last 30 Days
    @GetMapping("/hottest-day")
    public List<Document> getHottestDay() {
        return analyticsService.getHottestDayPerCity();
    }

    // Total Rainfall per City in Last 30 Days
    @GetMapping("/30days-total-rainfall")
    public List<Document> getTotalRainfall(){
        return analyticsService.getTotalRainfallPerCityLast30Days();
    }

    // TODO this is a test controller / service all mashed up.
    //  for now i'll keep this here to get some information if needed
    //  DO NOT BASE ANY FUNCTIONALITY ON THIS, it's here just for testing and debugging
    //  this will be completely deleted in the future
    @GetMapping("/TEST")
    public ResponseEntity<Object> TEST_ANALYTICS(@RequestParam String city){

        // MongoClient to connect to the deb
        try (MongoClient mongoClient = MongoClients.create(
                MongoClientSettings.builder()
                        .applyConnectionString(new ConnectionString("mongodb://localhost:27017"))
                        .build()
        )) {

            // Get a reference to the db
            MongoDatabase WeatherAppMongoDB = mongoClient.getDatabase("WeatherApp");


            // Get a reference to some collections
            MongoCollection<Document> eweCollection = WeatherAppMongoDB.getCollection("extreme_weather_events");
            MongoCollection<Document> measurementCollection = WeatherAppMongoDB.getCollection("hourly_measurements");
            MongoCollection<Document> cityCollection = WeatherAppMongoDB.getCollection("cities");

            System.out.println("COLLECTION CONNECTION TEST ========================================================================");
            // Count and print the number of documents in each collection
            System.out.println("Number of documents in 'extreme_weather_events': " + eweCollection.countDocuments());
            System.out.println("Number of documents in 'hourly_measurements': " + measurementCollection.countDocuments());
            System.out.println("Number of documents in 'cities': " + cityCollection.countDocuments());

            // Cursor test
            System.out.println("CURSOR TEST 1 ========================================================================");
            try (MongoCursor<Document> cityCursor = cityCollection.find(
                    and(gt("lat", 10.0), lte("lat", 50.0)))
                    .iterator())
            {
                while (cityCursor.hasNext()) {
                    System.out.println(cityCursor.next().toJson());
                }
            }

            System.out.println("CURSOR TEST 2 ========================================================================");
            try (MongoCursor<Document> cityCursor = cityCollection.find(
                    and(eq("name", "Arezzo")))
                    .iterator())
            {
                while (cityCursor.hasNext()) {
                    System.out.println(cityCursor.next().toJson());
                }
            }

            System.out.println("CURSOR TEST 3 ========================================================================");
            try (MongoCursor<Document> cityCursor = cityCollection.find(
                            and(eq("name", city)))
                    .iterator())
            {
                while (cityCursor.hasNext()) {
                    System.out.println(cityCursor.next().toJson());
                }
            }

            System.out.println("AGGREGATE TEST 1 ========================================================================");
            // Finds all measurements from arezzo
            // TODO fai funzione che chiama api, convert in dto e poi genera cityId
            //  String cityId = CityUtility.generateCityId(...);
            String cityId = "are-tos-43,4633-11,879";
            measurementCollection.aggregate(
                    List.of(
                            // group by city, just count
                            group("$cityId", sum("number of measurements", 1))
                    )
            // Prints the result of the aggregation operation as JSON
            ).forEach(doc -> System.out.println(doc.toJson()));

            System.out.println("AGGREGATE TEST 2 ========================================================================");
            LocalDateTime startLdt = LocalDateTime.parse("2024-01-01T00:00:00");
            LocalDateTime endLdt = LocalDateTime.parse("2024-01-07T00:00:00");
            Date startDate = Date.from(startLdt.toInstant(ZoneOffset.UTC));
            Date endDate = Date.from(endLdt.toInstant(ZoneOffset.UTC));
            measurementCollection.aggregate(
                    Arrays.asList(
                            // select only the ones where the date is between start and end
                            match(and(
                                    gte("time", startDate),
                                    lt("time", endDate)
                            )),
                            // group by city, just count
                            group("$cityId", sum("number of measurements", 1)),
                            // Sort by the number of found measurements, then by city name
                            sort(orderBy(
                                    descending("number of measurements"),
                                    ascending("_id")
                            )),
                            // to change the "_id" field name to "city_id"
                            project(fields(
                                    computed("city_id", "$_id"),
                                    include("number of measurements")
                            ))
                    )
            // Prints the result of the aggregation operation as JSON
            ).forEach(doc -> System.out.println(doc.toJson()));

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body("hi");
        }
        catch (Exception e){
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    // TODO
    //  Put here all sort of analytics stuff, i say the most, the better

    // TODO
    //  Surely the aggregations must be done here, a note on them:
    //      Java driver are available only on MongoCollection entities, so we have
    //      to make a service that works on them instead of using the normal repositories

    // TODO
    //  Then maybe the most basic queries can be directly done calling repository methods (like find most ...)

}
