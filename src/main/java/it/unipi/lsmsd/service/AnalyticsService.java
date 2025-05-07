package it.unipi.lsmsd.service;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Accumulators.*;
import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Sorts.*;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import it.unipi.lsmsd.model.ExtremeWeatherEventCategory;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class AnalyticsService{

    @Autowired
    private final MongoClient mongoClient;
    private final MongoDatabase database;
    private final MongoCollection<Document> eweCollection;
    private final MongoCollection<Document> measurementCollection;
    private final MongoCollection<Document> cityCollection;

    private MongoDatabase getDatabase() {
        return mongoClient.getDatabase("WeatherApp");
    }

    public AnalyticsService() {
        this.mongoClient = MongoClients.create(
                MongoClientSettings.builder()
                        .applyConnectionString(new ConnectionString("mongodb://localhost:27017"))
                        .build()
        );
        this.database = mongoClient.getDatabase("WeatherApp");
        this.eweCollection = database.getCollection("extreme_weather_events");
        this.measurementCollection = database.getCollection("hourly_measurements");
        this.cityCollection = database.getCollection("cities");
    }

    /**
     * Retrieves the top cities most affected by extreme weather events (EWEs) of a specified category,
     * ranked by the total number of occurrences across all time.
     *
     * @param maxNumCitiesToFind the maximum number of top cities to return
     * @param EweCategory        the category of extreme weather events to consider
     * @return a list of documents, each containing a city identifier and the corresponding count of EWEs
     */

    public List<Document> topCitiesMostAffectedByEwe(
            int maxNumCitiesToFind,
            ExtremeWeatherEventCategory EweCategory
    ){
        return StreamSupport.stream(eweCollection.aggregate(
                Arrays.asList(
                        // select only the EWEs of the given category
                        match(
                                eq("category", EweCategory.name())
                        ),

                        // group by city (cityId) and count the number of EWEs found
                        group(
                                "$cityId",
                                // And count the number
                                sum("EWE count", 1)
                        ),

                        // Sort by the number of found EWEs
                        sort(orderBy(
                                descending("EWE count")
                        )),

                        // Limit the result to only top maxNumCitiesToFind
                        limit(maxNumCitiesToFind)
                )).spliterator(), false)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves the top cities most affected by extreme weather events (EWEs) of a specified category
     * within a given time range. The cities are ranked by the number of EWE occurrences and limited
     * to a specified maximum number of results.
     *
     * @param maxNumCitiesToFind the maximum number of top cities to return
     * @param EweCategory        the category of extreme weather events to consider
     * @param startDate          the start of the time interval (inclusive)
     * @param endDate            the end of the time interval (inclusive)
     * @return a list of documents, each containing a city identifier and the corresponding count of EWEs
     */
    public List<Document> topCitiesMostAffectedByEweInTimeRange(
            int maxNumCitiesToFind,
            ExtremeWeatherEventCategory EweCategory,
            LocalDateTime startDate,
            LocalDateTime endDate
    ){
        return StreamSupport.stream(eweCollection.aggregate(
                Arrays.asList(
                        match(and(
                                // select only the EWEs of the given category
                                eq("category", EweCategory.name().toUpperCase()),

                                // Which started after the given start date
                                gte("dateStart", startDate),

                                // And ended before the given end date
                                // NOTE: this excludes all EWE with a NULL dateEnd, so the ones not finished, which is ok for this purpose
                                lte("dateEnd", endDate)
                        )),

                        // group by city (cityId) and count the number of EWEs found
                        group(
                                "$cityId",
                                // And count the number
                                sum("EWE count", 1)
                        ),

                        // Sort by the number of found EWEs
                        sort(
                                orderBy(descending("EWE count"))
                        ),

                        // Limit the result to only top maxNumCitiesToFind
                        limit(maxNumCitiesToFind)
                )).spliterator(), false)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves the number of measurements recorded for each city within a specified time interval.
     * The results are sorted in descending order by the count of measurements, and in ascending order
     * by city identifier in case of ties.
     *
     * @param startDate the start of the time interval (inclusive)
     * @param endDate   the end of the time interval (exclusive)
     * @return a list of documents, each containing a city identifier and the corresponding number of measurements
     */

    public List<Document> getMeasurementCountByCityInRange(
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        Date start = Date.from(startDate.toInstant(ZoneOffset.UTC));
        Date end = Date.from(endDate.toInstant(ZoneOffset.UTC));

        return StreamSupport.stream(measurementCollection.aggregate(
            Arrays.asList(
                    // select only the ones where the date is between start and end
                    match(and(
                            gte("time", start),
                            lt("time", end)
                    )),
                    // group by city, just count
                    group("$cityId", sum("number of measurements", 1)),

                    // Sort by the number of found measurements, then by city name
                    sort(orderBy(
                            descending("number of measurements"),
                            ascending("_id")
                    )),

                    project(fields(
                            computed("city_id", "$_id"),
                            include("number of measurements")
                    ))
            )).spliterator(), false)
            .collect(Collectors.toList());
    }

    /*
    * STATISTICS / INFORMATION ON SINGLE CITIES
    */

    /**
     * Computes the average rainfall measured in a specific city during a given time interval.
     *
     * @param cityId    the ID of the target city
     * @param startDate the start of the time interval (inclusive)
     * @param endDate   the end of the time interval (inclusive)
     * @return a list containing a single document with the average rainfall value,
     *      or an empty list if no data is found
     */
    public List<Document> averageRainfallInCityDuringPeriod(
            String cityId,
            LocalDateTime startDate,
            LocalDateTime endDate
    ){
        Date start = Date.from(startDate.toInstant(ZoneOffset.UTC));
        Date end = Date.from(endDate.toInstant(ZoneOffset.UTC));

        return StreamSupport.stream(measurementCollection.aggregate(
                        Arrays.asList(
                                match(and(
                                        // Select only measurements from the given city
                                        eq("cityId", cityId),

                                        // Within the specified time range
                                        gte("time", start),
                                        lte("time", end)
                                )),

                                // Group everything to compute a single average
                                group(
                                        null,
                                        avg("averageRainfall", "$rainfall")
                                )
                        )).spliterator(), false)
                .collect(Collectors.toList());

    }

    /**
     * Computes the average snowfall measured in a specific city during a given time interval.
     *
     * @param cityId    the ID of the target city
     * @param startDate the start of the time interval (inclusive)
     * @param endDate   the end of the time interval (inclusive)
     * @return a list containing a single document with the average snowfall value,
     *         or an empty list if no data is found
     */
    public List<Document> averageSnowfallInCityDuringPeriod(
            String cityId,
            LocalDateTime startDate,
            LocalDateTime endDate
    ){
        Date start = Date.from(startDate.toInstant(ZoneOffset.UTC));
        Date end = Date.from(endDate.toInstant(ZoneOffset.UTC));

        return StreamSupport.stream(measurementCollection.aggregate(
                Arrays.asList(
                        match(and(
                                // Select only measurements from the given city
                                eq("cityId", cityId),

                                // Within the specified time range
                                gte("time", start),
                                lte("time", end)
                        )),

                        // Group everything to compute a single average
                        group(
                                null,
                                avg("averageSnowfall", "$snowfall")
                        )
                )).spliterator(), false)
                .collect(Collectors.toList());
    }

    /**
     * Computes the average temperature measured in a specific city during a given time interval.
     *
     * @param cityId    the ID of the target city
     * @param startDate the start of the time interval (inclusive)
     * @param endDate   the end of the time interval (inclusive)
     * @return a list containing a single document with the average temperature value,
     *         or an empty list if no data is found
     */
    public List<Document> averageTemperatureInCityDuringPeriod(
            String cityId,
            LocalDateTime startDate,
            LocalDateTime endDate
    ){
        Date start = Date.from(startDate.toInstant(ZoneOffset.UTC));
        Date end = Date.from(endDate.toInstant(ZoneOffset.UTC));

        return StreamSupport.stream(measurementCollection.aggregate(
                Arrays.asList(
                        match(and(
                                // Select only measurements from the given city
                                eq("cityId", cityId),

                                // Within the specified time range
                                gte("time", start),
                                lte("time", end)
                        )),

                        // Group everything to compute a single average
                        group(
                                null,
                                avg("averageTemperature", "$temperature")
                        )
                )).spliterator(), false)
                .collect(Collectors.toList());
    }

    /**
     * Computes the maximum temperature measured in a specific city during a given time interval.
     *
     * @param cityId    the ID of the target city
     * @param startDate the start of the time interval (inclusive)
     * @param endDate   the end of the time interval (inclusive)
     * @return a list containing a single document with the maximum temperature value,
     *         or an empty list if no data is found
     */
    public List<Document> maxTemperatureInCityDuringPeriod(
            String cityId,
            LocalDateTime startDate,
            LocalDateTime endDate
    ){
        Date start = Date.from(startDate.toInstant(ZoneOffset.UTC));
        Date end = Date.from(endDate.toInstant(ZoneOffset.UTC));

        return StreamSupport.stream(measurementCollection.aggregate(
                Arrays.asList(
                        match(and(
                                // Select only measurements from the given city
                                eq("cityId", cityId),

                                // Within the specified time range
                                gte("time", start),
                                lte("time", end)
                        )),

                        // Group everything to compute the maximum temperature
                        group(
                                null,
                                max("maxTemperature", "$temperature")
                        )
                )).spliterator(), false)
                .collect(Collectors.toList());
    }

    /**
     * Computes the minimum temperature measured in a specific city during a given time interval.
     *
     * @param cityId    the ID of the target city
     * @param startDate the start of the time interval (inclusive)
     * @param endDate   the end of the time interval (inclusive)
     * @return a list containing a single document with the minimum temperature value,
     *         or an empty list if no data is found
     */
    public List<Document> minTemperatureInCityDuringPeriod(
            String cityId,
            LocalDateTime startDate,
            LocalDateTime endDate
    ){
        Date start = Date.from(startDate.toInstant(ZoneOffset.UTC));
        Date end = Date.from(endDate.toInstant(ZoneOffset.UTC));

        return StreamSupport.stream(measurementCollection.aggregate(
                Arrays.asList(
                        match(and(
                                // Select only measurements from the given city
                                eq("cityId", cityId),

                                // Within the specified time range
                                gte("time", start),
                                lte("time", end)
                        )),

                        // Group everything to compute the minimum temperature
                        group(
                                null,
                                min("minTemperature", "$temperature")
                        )
                )).spliterator(), false)
                .collect(Collectors.toList());
    }



    /*
     * STATISTICS / INFORMATION ACROSS CITIES
     */

    /**
     * Computes the top X cities with the highest average rainfall during a given time interval.
     * The average rainfall is calculated by dividing the total rainfall by the number of measurements.
     *
     * @param maxNumCitiesToFind the number of top cities to return
     * @param startDate          the start of the time interval (inclusive)
     * @param endDate            the end of the time interval (inclusive)
     * @return a list containing the top X cities with the highest average rainfall values,
     *         or an empty list if no data is found
     */
    public List<Document> topXRainiestCitiesDuringPeriod(
            int maxNumCitiesToFind,
            LocalDateTime startDate,
            LocalDateTime endDate
    ){
        Date start = Date.from(startDate.toInstant(ZoneOffset.UTC));
        Date end = Date.from(endDate.toInstant(ZoneOffset.UTC));

        return StreamSupport.stream(measurementCollection.aggregate(
                Arrays.asList(
                        match(and(
                                // Select measurements within the specified time range
                                gte("time", start),
                                lte("time", end)
                        )),

                        // Group by city, summing the total rainfall and counting the number of measurements
                        group(
                                "$cityId",
                                avg("averageRainfall", "$rainfall")
                        ),

                        // Sort by average rainfall in descending order
                        sort(orderBy(
                                descending("averageRainfall")
                        )),

                        // Limit to the top X cities with the highest average rainfall
                        limit(maxNumCitiesToFind)
                )).spliterator(), false)
                .collect(Collectors.toList());
    }

    /**
     * Computes the top X cities with the highest average snowfall during a given time interval.
     * The average snowfall is calculated by dividing the total snowfall by the number of measurements.
     *
     * @param maxNumCitiesToFind the number of top cities to return
     * @param startDate          the start of the time interval (inclusive)
     * @param endDate            the end of the time interval (inclusive)
     * @return a list containing the top X cities with the highest average snowfall values,
     *         or an empty list if no data is found
     */
    public List<Document> topXSnowiestCitiesDuringPeriod(
            int maxNumCitiesToFind,
            LocalDateTime startDate,
            LocalDateTime endDate
    ){
        Date start = Date.from(startDate.toInstant(ZoneOffset.UTC));
        Date end = Date.from(endDate.toInstant(ZoneOffset.UTC));

        return StreamSupport.stream(measurementCollection.aggregate(
                        Arrays.asList(
                                match(and(
                                        // Select measurements within the specified time range
                                        gte("time", start),
                                        lte("time", end)
                                )),

                                // Group by city, summing the total snowfall and counting the number of measurements
                                group(
                                        "$cityId",
                                        avg("averageSnowfall", "$snowfall")
                                ),

                                // Sort by average snowfall in descending order
                                sort(orderBy(
                                        descending("averageSnowfall")
                                )),

                                // Limit to the top X cities with the highest average snowfall
                                limit(maxNumCitiesToFind)
                        )).spliterator(), false)
                .collect(Collectors.toList());
    }

    /**
     * Computes the top X cities with the highest average temperature during a given time interval.
     * The average temperature is calculated by dividing the total temperature by the number of measurements.
     *
     * @param maxNumCitiesToFind the number of top cities to return
     * @param startDate          the start of the time interval (inclusive)
     * @param endDate            the end of the time interval (inclusive)
     * @return a list containing the top X cities with the highest average temperature values,
     *         or an empty list if no data is found
     */
    public List<Document> topXCitiesWithHighestAverageTemperature(
            int maxNumCitiesToFind,
            LocalDateTime startDate,
            LocalDateTime endDate
    ){
        Date start = Date.from(startDate.toInstant(ZoneOffset.UTC));
        Date end = Date.from(endDate.toInstant(ZoneOffset.UTC));

        return StreamSupport.stream(measurementCollection.aggregate(
                Arrays.asList(
                        match(and(
                                // Select measurements within the specified time range
                                gte("time", start),
                                lte("time", end)
                        )),

                        // Group by city, summing the total temperature and counting the number of measurements
                        group(
                                "$cityId",
                                avg("averageTemperature", "$temperature")
                        ),

                        // Sort by average temperature in descending order
                        sort(orderBy(
                                descending("averageTemperature")
                        )),

                        // Limit to the top X cities with the highest average temperature
                        limit(maxNumCitiesToFind)
                )).spliterator(), false)
                .collect(Collectors.toList());
    }

    /**
     * Computes the top X cities with the lowest average temperature during a given time interval.
     * The average temperature is calculated by dividing the total temperature by the number of measurements.
     *
     * @param maxNumCitiesToFind the number of top cities to return
     * @param startDate          the start of the time interval (inclusive)
     * @param endDate            the end of the time interval (inclusive)
     * @return a list containing the top X cities with the lowest average temperature values,
     *         or an empty list if no data is found
     */
    public List<Document> topXCitiesWithLowestAverageTemperature(
            int maxNumCitiesToFind,
            LocalDateTime startDate,
            LocalDateTime endDate
    ){
        Date start = Date.from(startDate.toInstant(ZoneOffset.UTC));
        Date end = Date.from(endDate.toInstant(ZoneOffset.UTC));

        return StreamSupport.stream(measurementCollection.aggregate(
                        Arrays.asList(
                                match(and(
                                        // Select measurements within the specified time range
                                        gte("time", start),
                                        lte("time", end)
                                )),

                                // Group by city, summing the total temperature and counting the number of measurements
                                group(
                                        "$cityId",
                                        avg("averageTemperature", "$temperature")
                                ),

                                // Sort by average temperature in ascending order (for lowest temperatures)
                                sort(orderBy(
                                        ascending("averageTemperature")
                                )),

                                // Limit to the top X cities with the lowest average temperature
                                limit(maxNumCitiesToFind)
                        )).spliterator(), false)
                .collect(Collectors.toList());
    }

    /**
     * Computes the top X cities with the highest recorded temperature during a given time interval.
     * The highest temperature is determined by selecting the maximum recorded temperature for each city.
     *
     * @param maxNumCitiesToFind the number of top cities to return
     * @param startDate          the start of the time interval (inclusive)
     * @param endDate            the end of the time interval (inclusive)
     * @return a list containing the top X cities with the highest recorded temperatures,
     *         or an empty list if no data is found
     */
    public List<Document> topXCitiesWithHighestRecordedTemperature(
            int maxNumCitiesToFind,
            LocalDateTime startDate,
            LocalDateTime endDate
    ){
        Date start = Date.from(startDate.toInstant(ZoneOffset.UTC));
        Date end = Date.from(endDate.toInstant(ZoneOffset.UTC));

        return StreamSupport.stream(measurementCollection.aggregate(
                Arrays.asList(
                        match(and(
                                // Select measurements within the specified time range
                                gte("time", start),
                                lte("time", end)
                        )),

                        // Group by city, finding the maximum recorded temperature
                        group(
                                "$cityId",
                                max("maxTemperature", "$temperature")
                        ),

                        // Sort by the highest recorded temperature in descending order
                        sort(orderBy(
                                descending("maxTemperature")
                        )),

                        // Limit to the top X cities with the highest recorded temperature
                        limit(maxNumCitiesToFind)
                )).spliterator(), false)
                .collect(Collectors.toList());
    }

    /**
     * Computes the top X cities with the lowest recorded temperature during a given time interval.
     * The lowest temperature is determined by selecting the minimum recorded temperature for each city.
     *
     * @param maxNumCitiesToFind the number of top cities to return
     * @param startDate          the start of the time interval (inclusive)
     * @param endDate            the end of the time interval (inclusive)
     * @return a list containing the top X cities with the lowest recorded temperatures,
     *         or an empty list if no data is found
     */
    public List<Document> topXCitiesWithLowestRecordedTemperature(
            int maxNumCitiesToFind,
            LocalDateTime startDate,
            LocalDateTime endDate
    ){
        Date start = Date.from(startDate.toInstant(ZoneOffset.UTC));
        Date end = Date.from(endDate.toInstant(ZoneOffset.UTC));

        return StreamSupport.stream(measurementCollection.aggregate(
                Arrays.asList(
                        match(and(
                                // Select measurements within the specified time range
                                gte("time", start),
                                lte("time", end)
                        )),

                        // Group by city, finding the minimum recorded temperature
                        group(
                                "$cityId",
                                min("minTemperature", "$temperature")
                        ),

                        // Sort by the lowest recorded temperature in ascending order
                        sort(orderBy(
                                ascending("minTemperature")
                        )),

                        // Limit to the top X cities with the lowest recorded temperature
                        limit(maxNumCitiesToFind)
                )).spliterator(), false)
                .collect(Collectors.toList());
    }



    /*
     * STATISTICS / INFORMATION FOR SINGLE REGIONS
     */

    /**
     * Computes the average temperature of a specific region during a given time interval.
     * The average temperature is calculated using MongoDB's $avg operator.
     *
     * @param region    the name of the target region
     * @param startDate the start of the time interval (inclusive)
     * @param endDate   the end of the time interval (inclusive)
     * @return the average temperature of the specified region, or an empty list if no data is found
     */
    public List<Document> averageTemperatureInCitiesOfRegionDuringPeriod(
            String region,
            LocalDateTime startDate,
            LocalDateTime endDate
    ){
        Date start = Date.from(startDate.toInstant(ZoneOffset.UTC));
        Date end = Date.from(endDate.toInstant(ZoneOffset.UTC));

        return StreamSupport.stream(measurementCollection.aggregate(
                Arrays.asList(
                        // Join the `hourly_measurements` collection with the `cities` collection on `cityId`
                        lookup("cities", "cityId", "id", "cityDetails"),

                        // Filter to select only the region we are interested in and measurements within the specified time range
                        match(and(
                                gte("time", start),
                                lte("time", end),
                                eq("cityDetails.region", region)  // Filter by region
                        )),

                        group(
                                "$cityId",  // Group by cityId
                                avg("averageTemperature", "$temperature") // Directly calculate the average temperature
                        ),

                        // Project final output with city details and calculated average rainfall
                        project(fields(
                                computed("averageRainfall", "$averageRainfall"),
                                include("cityDetails.name", "cityDetails.region",
                                        "cityDetails.latitude", "cityDetails.longitude")
                        ))
                )).spliterator(), false)
                .collect(Collectors.toList());
    }


    /**
     * Computes the average temperature of an entire region during a given time interval.
     * The average temperature is calculated directly using the `$avg` operator in MongoDB.
     *
     * @param region    the name of the target region
     * @param startDate the start of the time interval (inclusive)
     * @param endDate   the end of the time interval (inclusive)
     * @return the average temperature of the specified region, or an empty list if no data is found
     */
    public List<Document> averageTemperatureOfRegion(
            String region,
            LocalDateTime startDate,
            LocalDateTime endDate
    ){
        Date start = Date.from(startDate.toInstant(ZoneOffset.UTC));
        Date end = Date.from(endDate.toInstant(ZoneOffset.UTC));

        return StreamSupport.stream(measurementCollection.aggregate(
                Arrays.asList(
                        // Join the `hourly_measurements` collection with the `cities` collection on `cityId`
                        lookup("cities", "cityId", "id", "cityDetails"),

                        // Filter to select only the region we are interested in and measurements within the specified time range
                        match(and(
                                gte("time", start),
                                lte("time", end),
                                eq("cityDetails.region", region)  // Filter by region
                        )),

                        // Calculate the average temperature for the entire region
                        group(
                                "cityDetails.region",  // Group by region
                                avg("averageTemperature", "$temperature")
                        )
                )).spliterator(), false)
                .collect(Collectors.toList());
    }

    /**
     * Computes the average rainfall of a specific region during a given time interval.
     * All city details are included in the result.
     *
     * @param region    the name of the target region
     * @param startDate the start of the time interval (inclusive)
     * @param endDate   the end of the time interval (inclusive)
     * @return the average rainfall of the specified region along with city details,
     *         or an empty list if no data is found
     */
    public List<Document> averageRainfallInCitiesOfRegionDuringPeriod(
            String region,
            LocalDateTime startDate,
            LocalDateTime endDate
    ){
        Date start = Date.from(startDate.toInstant(ZoneOffset.UTC));
        Date end = Date.from(endDate.toInstant(ZoneOffset.UTC));

        return StreamSupport.stream(measurementCollection.aggregate(
                Arrays.asList(
                        // Join the `hourly_measurements` collection with the `cities` collection on `cityId`
                        lookup("cities", "cityId", "id", "cityDetails"),

                        // Filter to select only the region we are interested in and measurements within the specified time range
                        match(and(
                                gte("time", start),
                                lte("time", end),
                                eq("cityDetails.region", region)  // Filter by region
                        )),

                        group(
                                "$cityId",  // Group by cityId
                                avg("averageRainfall", "$rainfall") // Directly calculate the average rainfall
                        ),

                        // Project final output with city details and calculated average rainfall
                        project(fields(
                                computed("averageRainfall", "$averageRainfall"),
                                include("cityDetails.name", "cityDetails.region",
                                        "cityDetails.latitude", "cityDetails.longitude")
                        ))
                )).spliterator(), false)
                .collect(Collectors.toList());
    }

    /**
     * Computes the average rainfall of a specific region during a given time interval.
     *
     * @param region    the name of the target region
     * @param startDate the start of the time interval (inclusive)
     * @param endDate   the end of the time interval (inclusive)
     * @return the average rainfall of the specified region,
     *         or an empty list if no data is found
     */
    public List<Document> averageRainfallOfRegion(
            String region,
            LocalDateTime startDate,
            LocalDateTime endDate
    ){
        Date start = Date.from(startDate.toInstant(ZoneOffset.UTC));
        Date end = Date.from(endDate.toInstant(ZoneOffset.UTC));

        return StreamSupport.stream(measurementCollection.aggregate(
                Arrays.asList(
                        // Join the `hourly_measurements` collection with the `cities` collection on `cityId`
                        lookup("cities", "cityId", "id", "cityDetails"),

                        // Filter to select only the region we are interested in and measurements within the specified time range
                        match(and(
                                gte("time", start),
                                lte("time", end),
                                eq("cityDetails.region", region)  // Filter by region
                        )),

                        // Group by region and calculate the average rainfall using the $avg operator
                        group(
                                null,  // No specific grouping (we're interested in the whole region)
                                avg("averageRainfall", "$rainfall")
                        )
                )).spliterator(), false)
                .collect(Collectors.toList());
    }


    /**
     * Computes the average snowfall of a specific region during a given time interval.
     * All city details are included in the result.
     *
     * @param region    the name of the target region
     * @param startDate the start of the time interval (inclusive)
     * @param endDate   the end of the time interval (inclusive)
     * @return the average snowfall of the specified region along with city details,
     *         or an empty list if no data is found
     */
    public List<Document> averageSnowfallInCitiesOfRegionDuringPeriod(
            String region,
            LocalDateTime startDate,
            LocalDateTime endDate
    ){
        Date start = Date.from(startDate.toInstant(ZoneOffset.UTC));
        Date end = Date.from(endDate.toInstant(ZoneOffset.UTC));

        return StreamSupport.stream(measurementCollection.aggregate(
                Arrays.asList(
                        // Join the `hourly_measurements` collection with the `cities` collection on `cityId`
                        lookup("cities", "cityId", "id", "cityDetails"),

                        // Filter to select only the region we are interested in and measurements within the specified time range
                        match(and(
                                gte("time", start),
                                lte("time", end),
                                eq("cityDetails.region", region)  // Filter by region
                        )),

                        // Group by city
                        group(
                                "$cityId",  // Group by cityId
                                avg("averageSnowfall", "$snowfall")
                        ),

                        // Project final output with city details and calculated average rainfall
                        project(fields(
                                computed("averageSnowfall", "averageSnowfall"),
                                include("cityDetails.name", "cityDetails.region",
                                        "cityDetails.latitude", "cityDetails.longitude")
                        ))
                )).spliterator(), false)
                .collect(Collectors.toList());
    }

    /**
     * Computes the average snowfall of a specific region during a given time interval.
     *
     * @param region    the name of the target region
     * @param startDate the start of the time interval (inclusive)
     * @param endDate   the end of the time interval (inclusive)
     * @return the average snowfall of the specified region,
     *         or an empty list if no data is found
     */
    public List<Document> averageSnowfallOfRegion(
            String region,
            LocalDateTime startDate,
            LocalDateTime endDate
    ){
        Date start = Date.from(startDate.toInstant(ZoneOffset.UTC));
        Date end = Date.from(endDate.toInstant(ZoneOffset.UTC));

        return StreamSupport.stream(measurementCollection.aggregate(
                Arrays.asList(
                        // Join the `hourly_measurements` collection with the `cities` collection on `cityId`
                        lookup("cities", "cityId", "id", "cityDetails"),

                        // Filter to select only the region we are interested in and measurements within the specified time range
                        match(and(
                                gte("time", start),
                                lte("time", end),
                                eq("cityDetails.region", region)  // Filter by region
                        )),

                        // Group by region and calculate the average snowfall using the $avg operator
                        group(
                                null,  // No specific grouping (we're interested in the whole region)
                                avg("averageSnowfall", "$snowfall")
                        )
                )).spliterator(), false)
                .collect(Collectors.toList());
    }


    /*
     * STATISTICS / INFORMATION ACROSS REGIONS
     */

    /**
     * Computes the top X rainiest regions during a given time interval.
     * The rainfall is calculated by averaging the total rainfall for each region.
     *
     * @param maxNumRegionsToFind the number of top rainiest regions to return
     * @param startDate           the start of the time interval (inclusive)
     * @param endDate             the end of the time interval (inclusive)
     * @return the top X rainiest regions, ordered by average rainfall,
     *         or an empty list if no data is found
     */
    public List<Document> topXRainiestRegionsDuringPeriod(
            int maxNumRegionsToFind,
            LocalDateTime startDate,
            LocalDateTime endDate
    ){
        Date start = Date.from(startDate.toInstant(ZoneOffset.UTC));
        Date end = Date.from(endDate.toInstant(ZoneOffset.UTC));

        return StreamSupport.stream(measurementCollection.aggregate(
                Arrays.asList(
                        // Join the `hourly_measurements` collection with the `cities` collection on `cityId`
                        lookup("cities", "cityId", "id", "cityDetails"),

                        // Filter to select only measurements within the specified time range
                        match(and(
                                gte("time", start),
                                lte("time", end)
                        )),

                        // Group by region and calculate the average rainfall using the $avg operator
                        group(
                                "cityDetails.region",  // Group by region
                                avg("averageRainfall", "$rainfall")
                        ),

                        // Sort by the average rainfall in descending order
                        sort(orderBy(
                                descending("averageRainfall")
                        )),

                        // Limit the result to only the top maxNumRegionsToFind regions
                        limit(maxNumRegionsToFind)
                )).spliterator(), false)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves the top X regions with the highest average snowfall during a specified time range.
     *
     * @param maxNumRegionsToFind number of regions to return
     * @param startDate           start of the time interval (inclusive)
     * @param endDate             end of the time interval (inclusive)
     * @return list of documents containing region names and corresponding average snowfall values
     */
    public List<Document> topXSnowiestRegionsDuringPeriod(
            int maxNumRegionsToFind,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        Date start = Date.from(startDate.toInstant(ZoneOffset.UTC));
        Date end = Date.from(endDate.toInstant(ZoneOffset.UTC));

        return StreamSupport.stream(measurementCollection.aggregate(
                Arrays.asList(
                        // Join with the cities collection to access region info
                        lookup("cities", "cityId", "id", "cityDetails"),

                        // Filter measurements by time range
                        match(and(
                                gte("time", start),
                                lte("time", end)
                        )),

                        // Group by region and calculate average snowfall
                        group("cityDetails.region", avg("averageSnowfall", "$snowfall")),

                        // Sort by average snowfall descending
                        sort(descending("averageSnowfall")),

                        // Limit to top X regions
                        limit(maxNumRegionsToFind)
                )).spliterator(), false)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves the top X regions with the highest average temperature during a specified time interval.
     *
     * @param maxNumRegionsToFind number of regions to return
     * @param startDate           start of the time interval (inclusive)
     * @param endDate             end of the time interval (inclusive)
     * @return list of documents containing region names and corresponding average temperature values
     */
    public List<Document> topXHottestRegionsDuringPeriod(
            int maxNumRegionsToFind,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        Date start = Date.from(startDate.toInstant(ZoneOffset.UTC));
        Date end = Date.from(endDate.toInstant(ZoneOffset.UTC));

        return StreamSupport.stream(measurementCollection.aggregate(
                Arrays.asList(
                        // Join with the cities collection to access region info
                        lookup("cities", "cityId", "id", "cityDetails"),

                        // Filter measurements by time range
                        match(and(
                                gte("time", start),
                                lte("time", end)
                        )),

                        // Group by region and calculate average temperature
                        group("cityDetails.region", avg("averageTemperature", "$temperature")),

                        // Sort by average temperature descending
                        sort(descending("averageTemperature")),

                        // Limit to top X regions
                        limit(maxNumRegionsToFind)
                )).spliterator(), false)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves the top X regions with the lowest average temperature during a specified time interval.
     *
     * @param maxNumRegionsToFind number of regions to return
     * @param startDate           start of the time interval (inclusive)
     * @param endDate             end of the time interval (inclusive)
     * @return list of documents containing region names and corresponding average temperature values
     */
    public List<Document> topXColdestRegionsDuringPeriod(
            int maxNumRegionsToFind,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        Date start = Date.from(startDate.toInstant(ZoneOffset.UTC));
        Date end = Date.from(endDate.toInstant(ZoneOffset.UTC));

        return StreamSupport.stream(measurementCollection.aggregate(
                Arrays.asList(
                        // Join with the cities collection to access region info
                        lookup("cities", "cityId", "id", "cityDetails"),

                        // Filter measurements by time range
                        match(and(
                                gte("time", start),
                                lte("time", end)
                        )),

                        // Group by region and calculate average temperature
                        group("cityDetails.region", avg("averageTemperature", "$temperature")),

                        // Sort by average temperature ascending
                        sort(ascending("averageTemperature")),

                        // Limit to top X regions
                        limit(maxNumRegionsToFind)
                )).spliterator(), false)
                .collect(Collectors.toList());
    }


	 // Average Temperature per City for Last 30 Days with City Info
    public List<Document> getAvgTemperaturePerCityLast30Days() {

        // Get the current time in UTC
        ZonedDateTime thirtyDaysAgoUTC = ZonedDateTime.now(ZoneOffset.UTC).minusDays(30);
        Date thirtyDaysAgo = Date.from(thirtyDaysAgoUTC.toInstant());

        // Get the collection
        MongoCollection<Document> collection = getDatabase().getCollection("hourly_measurements");

        // Aggregation pipeline
        List<Bson> pipeline = List.of(
                match(gte("time", thirtyDaysAgo)),  // Match documents from the last 30 days
                group("$cityId", avg("avgTemperature", "$temperature")),  // Group by cityId and calculate average temperature
                lookup("cities", "_id", "_id", "cityDetails"),  // Lookup city details
                unwind("$cityDetails"),  // Unwind the cityDetails array
                project(fields(
                        include("avgTemperature"),
                        computed("cityName", "$cityDetails.name"),
                        computed("cityRegion", "$cityDetails.region")
                ))
        );

        // Execute aggregation
        return collection.aggregate(pipeline).into(new java.util.ArrayList<>());
    }

    // Hottest Day for each city
    public List<Document> getHottestDayPerCity() {

        // Get the collection
        MongoCollection<Document> collection = getDatabase().getCollection("hourly_measurements");

        // Aggregation pipeline
        List<Bson> pipeline = List.of(
                group("$cityId", max("maxTemperature", "$temperature")),  // Group by cityId and pick max temperature
                lookup("cities", "_id", "_id", "cityDetails"),  // Lookup city details
                unwind("$cityDetails"),  // Unwind the cityDetails array
                project(fields(
                        include("maxTemperature"),
                        computed("cityName", "$cityDetails.name"),
                        computed("cityRegion", "$cityDetails.region")
                ))
        );

        // Execute aggregation
        return collection.aggregate(pipeline).into(new java.util.ArrayList<>());
    }

    // Total Rainfall per City in Last 30 Days
    public List<Document> getTotalRainfallPerCityLast30Days() {

        // Get the current time in UTC
        ZonedDateTime thirtyDaysAgoUTC = ZonedDateTime.now(ZoneOffset.UTC).minusDays(30);
        Date thirtyDaysAgo = Date.from(thirtyDaysAgoUTC.toInstant());

        // Get the collection
        MongoCollection<Document> collection = getDatabase().getCollection("hourly_measurements");

        // Aggregation pipeline
        List<Bson> pipeline = List.of(
                match(gte("time", thirtyDaysAgo)),  // Match documents from the last 30 days
                group("$cityId", sum("totalRainfall", "$rainfall")),  // Group by cityId and sum rainfall
                lookup("cities", "_id", "_id", "cityDetails"),  // Lookup city details
                unwind("$cityDetails"),  // Unwind the cityDetails array
                project(fields(
                        include("totalRainfall"),
                        computed("cityName", "$cityDetails.name"),
                        computed("cityRegion", "$cityDetails.region")
                ))
        );

        // Execute aggregation
        return collection.aggregate(pipeline).into(new java.util.ArrayList<>());
    }

}
