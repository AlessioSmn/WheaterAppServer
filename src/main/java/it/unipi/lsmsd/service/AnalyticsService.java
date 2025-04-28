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
    private MongoClient mongoClient;
	
	private MongoDatabase getDatabase() {
        return mongoClient.getDatabase("WeatherApp");
    }
    // private final MongoDatabase database;
    // private final MongoCollection<Document> eweCollection;
    // private final MongoCollection<Document> measurementCollection;
    // private final MongoCollection<Document> cityCollection;

    // public AnalyticsService() {
    //     this.mongoClient = MongoClients.create(
    //             MongoClientSettings.builder()
    //                     .applyConnectionString(new ConnectionString("mongodb://localhost:27017"))
    //                     .build()
    //     );
    //     this.database = mongoClient.getDatabase("WeatherApp");
    //     this.eweCollection = database.getCollection("extreme_weather_events");
    //     this.measurementCollection = database.getCollection("hourly_measurements");
    //     this.cityCollection = database.getCollection("cities");
    // }

    public List<Document> topCitiesMostAffectedByEwe(
            int maxNumCitiesToFind,
            ExtremeWeatherEventCategory EweCategory
    ){
		MongoCollection<Document> collection = getDatabase().getCollection("extreme_weather_events");
        return StreamSupport.stream(collection.aggregate(
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

    public List<Document> topCitiesMostAffectedByEweInTimeRange(
            int maxNumCitiesToFind,
            ExtremeWeatherEventCategory EweCategory,
            LocalDateTime startDate,
            LocalDateTime endDate
    ){
		MongoCollection<Document> collection = getDatabase().getCollection("extreme_weather_events");
        return StreamSupport.stream(collection.aggregate(
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

    public List<Document> getMeasurementCountByCityInRange(LocalDateTime startDate, LocalDateTime endDate) {
        Date start = Date.from(startDate.toInstant(ZoneOffset.UTC));
        Date end = Date.from(endDate.toInstant(ZoneOffset.UTC));

		MongoCollection<Document> collection = getDatabase().getCollection("hourly_measurements");
        return StreamSupport.stream(collection.aggregate(
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
