package it.unipi.lsmsd.repository;

// import it.unipi.lsmsd.dto.AvgTemperaturePerCityDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

import org.bson.Document;

@Repository
public class AnalyticsRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    // Average Temperature per City for Last 30 Days with City Info
	public List<Document> getAvgTemperaturePerCityLast30Days() {

		// Get the current time in UTC
		ZonedDateTime thirtyDaysAgoUTC = ZonedDateTime.now(ZoneOffset.UTC).minusDays(30);
		Date thirtyDaysAgo = Date.from(thirtyDaysAgoUTC.toInstant());

		// Stage 1: Match documents from last 30 days
		MatchOperation matchLast30Days = Aggregation.match(Criteria.where("time").gte(thirtyDaysAgo));

		// Stage 2: Group by cityId and calculate average temperature
		GroupOperation groupByCity = Aggregation.group("cityId")
				.avg("temperature").as("avgTemperature");

		// Stage 3: Lookup city details from the "cities" collection
		LookupOperation lookupCityDetails = Aggregation.lookup(
				"cities",     // from collection "cities"
				"_id",        // localField (_id after grouping is cityId)
				"_id",        // foreignField (_id in cities collection)
				"cityDetails" // as
		);

		// Stage 4: Unwind the cityDetails array (lookup returns an array)
		UnwindOperation unwindCityDetails = Aggregation.unwind("cityDetails");

		// Stage 5: Project the fields you want
		ProjectionOperation project = Aggregation.project()
				.and("cityDetails.name").as("cityName")
				.and("cityDetails.region").as("cityRegion")
				.andInclude("avgTemperature")
				.andExclude("_id");

		// Build the aggregation pipeline
		Aggregation aggregation = Aggregation.newAggregation(
				matchLast30Days,
				groupByCity,
				lookupCityDetails,
				unwindCityDetails,
				project
		);

		AggregationResults<Document> results = mongoTemplate.aggregate(
				aggregation,
				"hourly_measurements", // input collection
				Document.class
		);

		return results.getMappedResults();
	}
    
	// Hottest Day for each city
	public List<Document> getHottestDayPerCity() {

		// Stage 1: Group by cityId and pick the measurement with max temperature
		GroupOperation groupByCity = Aggregation.group("cityId")
				.max("temperature").as("maxTemperature");
	
		// Stage 2: Lookup city details from cities collection
		LookupOperation lookupCityDetails = Aggregation.lookup(
				"cities",     // from collection "cities"
				"_id",        // localField (_id after grouping is cityId)
				"_id",        // foreignField (_id in cities collection)
				"cityDetails" // as
		);
	
		// Stage 3: Unwind the cityDetails array (since lookup returns array)
		UnwindOperation unwindCityDetails = Aggregation.unwind("cityDetails");
	
		// Stage 4: Project required fields
		ProjectionOperation project = Aggregation.project()
				.and("cityDetails.name").as("cityName")
				.and("cityDetails.region").as("cityRegion")
				.andInclude("maxTemperature")
				.andExclude("_id");
	
		// Build aggregation
		Aggregation aggregation = Aggregation.newAggregation(
				groupByCity,
				lookupCityDetails,
				unwindCityDetails,
				project
		);
	
		AggregationResults<Document> results = mongoTemplate.aggregate(
				aggregation,
				"hourly_measurements", // input collection
				Document.class
		);
	
		return results.getMappedResults();
	}

	// Total Rainfall per City in Last 30 Days
	public List<Document> getTotalRainfallPerCityLast30Days() {

		// Get the current time in UTC
		ZonedDateTime thirtyDaysAgoUTC = ZonedDateTime.now(ZoneOffset.UTC).minusDays(30);
		Date thirtyDaysAgo = Date.from(thirtyDaysAgoUTC.toInstant());

		// Stage 1: Match documents from last 30 days
		MatchOperation matchLast30Days = Aggregation.match(Criteria.where("time").gte(thirtyDaysAgo));

		// Stage 2: Group by cityId and sum rainfall
		GroupOperation groupByCity = Aggregation.group("cityId")
				.sum("rainfall").as("totalRainfall");

		// Stage 3: Lookup city details
		LookupOperation lookupCityDetails = Aggregation.lookup(
				"cities",
				"_id",   // localField (_id is cityId after grouping)
				"_id",   // foreignField (_id in cities)
				"cityDetails"
		);

		// Stage 4: Unwind cityDetails
		UnwindOperation unwindCityDetails = Aggregation.unwind("cityDetails");

		// Stage 5: Project required fields
		ProjectionOperation project = Aggregation.project()
				.and("cityDetails.name").as("cityName")
				.and("cityDetails.region").as("cityRegion")
				.andInclude("totalRainfall")
				.andExclude("_id");

		Aggregation aggregation = Aggregation.newAggregation(
				matchLast30Days,
				groupByCity,
				lookupCityDetails,
				unwindCityDetails,
				project
		);

		AggregationResults<Document> results = mongoTemplate.aggregate(
				aggregation,
				"hourly_measurements",
				Document.class
		);

		return results.getMappedResults();
	}

}