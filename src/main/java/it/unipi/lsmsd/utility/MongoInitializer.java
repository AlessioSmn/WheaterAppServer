package it.unipi.lsmsd.utility;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.TimeSeriesOptions;
import com.mongodb.client.model.TimeSeriesGranularity;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

//  
// Spring Boot CommandLineRunner allows custom logic execution at startup
// It executes code after the application context is loaded and right before the application starts
@Component
public class MongoInitializer implements CommandLineRunner {

    private final MongoClient mongoClient;
    
    // Get database name from application.yml
    @Value("${spring.data.mongodb.database}") 
    private String databaseName;

    public MongoInitializer(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    // Checks if the hourlyMeasurements collection exists and creates it if it doesn’t, with time-series options
    // This was necessary cause MongoDb cannot auto create time-series collection unlike the usual collections when not found in DB
    @Override
    public void run(String... args) throws Exception {
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        String collectionName = "hourly_measurements";

        try {

            // Check if collection exists
            boolean collectionExists = database.listCollectionNames().into(new ArrayList<>()).contains(collectionName);

            if (!collectionExists) {
                // Create time-series options
                
                /* 
                    Time-Series Collection MongoDB is a specialized collection optimized for storing and querying time-based data, 
                        where each document records a measurement or event at a specific point in time
                * MetaField - store information related measurement but not part of the measurement itself 
                    Indexed automatically, faster queries based on metadata
                * Granularity - affects how MongoDB groups and stores data internally. MongoDB automatically optimizes data storage by 
                    partitioning data into time buckets, and granularity helps determine the bucket size.
                * 
                */
                TimeSeriesOptions timeSeriesOptions = new TimeSeriesOptions("time") // Time field
                                                        .metaField("cityId") // Metadata field that helps in index
                                                        .granularity(TimeSeriesGranularity.HOURS); // Granularity
                CreateCollectionOptions collectionOptions = new CreateCollectionOptions()
                                                            .timeSeriesOptions(timeSeriesOptions);

                // Create collection
                database.createCollection(collectionName, collectionOptions);
                // TODO: LOG
                System.out.println("Time-series collection '" + collectionName + "' has been created.");
            } else {
                // TODO: LOG
                System.out.println("Collection '" + collectionName + "' already exists.");
            }
            
        } catch (com.mongodb.MongoException ex) {
            System.err.println("MongoDB error occurred: " + ex.getMessage());
            // TODO: LOG
        } catch (IllegalArgumentException ex) {
            System.err.println("Invalid argument: " + ex.getMessage());
            // TODO: LOG
        } catch (Exception ex) {
            System.err.println("An unexpected error occurred: " + ex.getMessage());
            // TODO: LOG
        }

    }
}