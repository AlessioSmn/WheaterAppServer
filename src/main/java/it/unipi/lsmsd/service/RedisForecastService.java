package it.unipi.lsmsd.service;

import it.unipi.lsmsd.DTO.APIResponseDTO;
import it.unipi.lsmsd.model.City;
import it.unipi.lsmsd.repository.CityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import it.unipi.lsmsd.DTO.CityDTO;
import it.unipi.lsmsd.DTO.HourlyMeasurementDTO;
import it.unipi.lsmsd.utility.CityUtility;
import it.unipi.lsmsd.utility.CityBucketResolver;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

import java.io.IOException;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class RedisForecastService {
    // Constant Jedis connection pool instance of Redis running on localhost and 
    // default Redis port 6379 to manage connections
    @Autowired
    private JedisPool jedisPool;
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private DataHarvestService dataHarvestService;

    private static final double EARTH_RADIUS_KM = 6371.0;

    private static final int FORECAST_DAYS = 7;

    // <editor-fold desc="Utility functions">

    /**
     * Calculates the great-circle distance between two points on the Earth's surface using the Haversine formula.
     * <p>
     * This method accounts for the Earth's curvature and provides an approximation of the shortest distance
     * over the earth’s surface between two geographic coordinates.
     *
     * @param lat1 the latitude of the first point in decimal degrees
     * @param lon1 the longitude of the first point in decimal degrees
     * @param lat2 the latitude of the second point in decimal degrees
     * @param lon2 the longitude of the second point in decimal degrees
     * @return the distance between the two points in kilometers
     */
    public static double haversine(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    // </editor-fold>

    // <editor-fold desc="Direct access to redis (save and delete)">

    /**
     * Retrieves the weather forecast for a specified city from the Open-Meteo API and stores it in Redis.
     * <p>
     * This method performs the following steps:
     * <ul>
     *     <li>Retrieves the city information from the database using the provided city ID.</li>
     *     <li>If the city exists, requests the weather forecast from the Open-Meteo service.</li>
     *     <li>Sets the city ID on the retrieved forecast data and stores it in Redis.</li>
     * </ul>
     * If the city does not exist or a JSON processing error occurs, the method terminates silently.
     *
     * @param cityId the unique identifier of the city for which the forecast should be refreshed
     */
    public void refreshForecastAutomaticFromOpenMeteo(String cityId) {

        Optional<City> optionalCity = cityRepository.findById(cityId);
        if(optionalCity.isEmpty()){
            return;
        }
        City city = optionalCity.get();

        try {
            // Get Forecast from Open-Meteo
            APIResponseDTO responseDTO = dataHarvestService.getCityForecast(
                    city.getLatitude(),
                    city.getLongitude(),
                    0,
                    FORECAST_DAYS
            );

            HourlyMeasurementDTO hourlyMeasurementDTO = responseDTO.getHourly();

            hourlyMeasurementDTO.setCityId(cityId);
            // Save the forecast in Redis
            saveForecast(hourlyMeasurementDTO);
        }
        catch(JsonProcessingException ignored){

        }
    }

    /**
     * Splits an hourly forecast into separate daily forecasts and stores each one in Redis.
     * <p>
     * The input {@link HourlyMeasurementDTO} is divided based on the date portion of the timestamp,
     * and each resulting daily forecast is serialized to JSON and saved under a Redis key following the format:
     * {@code forecast:{bucket}:{cityId}:{date}}. Each entry is given a time-to-live (TTL) of 24 hours.
     *
     * @param dto the {@link HourlyMeasurementDTO} containing the full hourly forecast data to be persisted
     * @throws JsonProcessingException if an error occurs during the JSON serialization of any daily forecast
     */
    public void saveForecast(HourlyMeasurementDTO dto) throws JsonProcessingException {
        try (Jedis jedis = jedisPool.getResource()) {
            // 1. Split input DTO into daily DTOs
            Map<String, HourlyMeasurementDTO> dailyDTOs = new LinkedHashMap<>();
            List<String> timeList = dto.getTime();
            for (int i = 0; i < timeList.size(); i++) {
                String dateTime = timeList.get(i);
                String day = dateTime.split("T")[0];
    
                // Initialize DTO for the day if not already present
                dailyDTOs.computeIfAbsent(day, d -> {
                    HourlyMeasurementDTO dayDTO = new HourlyMeasurementDTO();
                    dayDTO.setTime(new ArrayList<>());
                    dayDTO.setTemperature(new ArrayList<>());
                    dayDTO.setRain(new ArrayList<>());
                    dayDTO.setSnowfall(new ArrayList<>());
                    dayDTO.setWindspeed(new ArrayList<>());
                    return dayDTO;
                });
    
                // Populate the daily DTO
                HourlyMeasurementDTO dailyDTO = dailyDTOs.get(day);
                dailyDTO.getTime().add(dateTime);
                dailyDTO.getTemperature().add(dto.getTemperature().get(i));
                dailyDTO.getRain().add(dto.getRain().get(i));
                dailyDTO.getSnowfall().add(dto.getSnowfall().get(i));
                dailyDTO.getWindspeed().add(dto.getWindspeed().get(i));
            }
    
            // 2. Store each daily DTO in Redis    
            for (Map.Entry<String, HourlyMeasurementDTO> entry : dailyDTOs.entrySet()) {
                String day = entry.getKey();
                HourlyMeasurementDTO dayDTO = entry.getValue();
                String redisKey = String.format("forecast:{%s}:%s:%s", CityBucketResolver.getBucket(dto.getCityId()), dto.getCityId(), day);
                String json = mapper.writeValueAsString(dayDTO);
    
                jedis.set(redisKey, json);                 // Save to Redis
                jedis.expire(redisKey, 86400);             // Set TTL: 24 hours
            }
        }
    }

    /**
     * Deletes all forecast entries stored in Redis.
     * <p>
     * The method scans the Redis keyspace for all keys matching the pattern {@code forecast:*}
     * and deletes them in batches. It uses a cursor-based scan to efficiently iterate through keys
     * without blocking the Redis server.
     */
    public void deleteAllForecast() {
        try (Jedis jedis = jedisPool.getResource()) {
            String cursor = "0";
            String pattern = "forecast:*";
            do {
                ScanResult<String> scanResult = jedis.scan(cursor, new ScanParams().match(pattern).count(100));
                List<String> keys = scanResult.getResult();
                if (!keys.isEmpty()) {
                    jedis.del(keys.toArray(new String[0]));
                }
                cursor = scanResult.getCursor();
            } while (!cursor.equals("0"));
        }
    }

    // </editor-fold>

    // <editor-fold desc="Forecast functions">

    /**
     * Retrieves the weather forecast from Redis for a specified city and target date.
     * <p>
     * The method constructs the Redis key using the provided city ID and date,
     * then attempts to retrieve the corresponding forecast data.
     * If the key does not exist in Redis, the method returns {@code null}.
     *
     * @param cityId the unique identifier of the city for which the forecast is requested
     * @param targetDate the {@link LocalDate} representing the day for which the forecast is desired (in UTC)
     * @return a JSON-formatted string containing the forecast for the specified date, or {@code null} if the data is not present in Redis
     */
    public String getForecastTargetDay(String cityId, LocalDate targetDate){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        try (Jedis jedis = jedisPool.getResource()) {

            String redisKey = String.format("forecast:{%s}:%s:%s", CityBucketResolver.getBucket(cityId), cityId, targetDate.format(formatter));

            return jedis.get(redisKey);
        }
    }

    // Get full 7-day forecast
    public String get7DayForecast(String cityId) throws IOException {

        try (Jedis jedis = jedisPool.getResource()) {  
            //Define date format for Redis key (yyyy-MM-dd) and get current date
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd"); 
            LocalDate currentDate = LocalDate.now();
            
            // List to hold the 7-day forecast data
            List<HourlyMeasurementDTO> allDaysData = new ArrayList<>();
    
            //Loop through the next 7 days (including today)
            for (int i = 0; i < 7; i++) {
                String dayKey = currentDate.plusDays(i).format(formatter);  // Format the date for each day (e.g., "2025-03-15")
                String redisKey = String.format("forecast:{%s}:%s:%s", CityBucketResolver.getBucket(cityId), cityId, dayKey); // Construct the Redis key using cityId and the date

                // Retrieve the forecast JSON for that day from Redis
                String json = jedis.get(redisKey);
                
                // If the data for that day exists in Redis, deserialize it into HourlyMeasurementDTO
                if (json != null) {
                    HourlyMeasurementDTO dayDTO = mapper.readValue(json, HourlyMeasurementDTO.class);  // Deserialize the JSON
                    allDaysData.add(dayDTO);  // Add the day’s data to the list
                }
            }
    
            // 11. Serialize the list of 7-day forecast data as a JSON array and return it
            return mapper.writeValueAsString(allDaysData);
        }
    }

    /**
     * Computes an estimated 24-hour weather forecast for an arbitrary geographic location on a specified date,
     * using a weighted interpolation of nearby cities' forecasts stored in Redis.
     * <p>
     * The method normalizes the region name, retrieves a list of cities from Redis that belong to the given region,
     * and calculates the forecast by weighting each city's data inversely by its distance from the target coordinates.
     * The final forecast values are averaged using these weights. If no data is found for a city, it is skipped.
     *
     * @param region the name of the region to which the surrounding cities belong
     * @param latitude the geographic latitude of the target location
     * @param longitude the geographic longitude of the target location
     * @param targetDay the {@link LocalDate} (in UTC) for which the forecast is requested
     * @return a JSON-formatted string representing the 24-hour interpolated forecast for the specified location and date
     */
    public String getForecastArbitraryCityTargetDay(
            String region,
            Double latitude,
            Double longitude,
            LocalDate targetDay
    ) {
        // Need to have first letter uppercase, last in lowercase
        region = region.substring(0, 1).toUpperCase() + region.substring(1).toLowerCase();

        String redisRegionKey = "region:" + region;

        List<City> targetCities = new ArrayList<>();
        try (Jedis jedis = jedisPool.getResource()) {
            Set<String> cityKeys = jedis.smembers(redisRegionKey);

            for (String cityKey : cityKeys) {
                Map<String, String> cityHash = jedis.hgetAll(cityKey);

                if (!cityHash.isEmpty()) {
                    City city = new City();
                    city.setName(cityHash.get("name"));
                    city.setRegion(cityHash.get("region"));
                    city.setId(cityKey.split(":")[1]);
                    String[] parts = cityKey.split("-");
                    city.setLatitude(Double.parseDouble(parts[2]));
                    city.setLongitude(Double.parseDouble(parts[3]));
                    targetCities.add(city);
                }
            }
        }

        double[] rainSum = new double[24];
        double[] snowSum = new double[24];
        double[] tempSum = new double[24];
        double[] windSum = new double[24];
        double weightSum = 0.0;

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode distancesArray = mapper.createArrayNode();

        for (City city : targetCities) {
            double distance = haversine(latitude, longitude, city.getLatitude(), city.getLongitude());
            double weight = distance == 0 ? 1000 : 1000 / distance;
            weightSum += weight;

            ObjectNode cityDistance = mapper.createObjectNode();
            cityDistance.put("city", city.getName());
            cityDistance.put("latitude", city.getLatitude());
            cityDistance.put("longitude", city.getLongitude());
            cityDistance.put("distance_km", distance);
            cityDistance.put("weight", weight);
            distancesArray.add(cityDistance);

            String json = getForecastTargetDay(city.getId(), targetDay);
            if (json == null || json.isEmpty()) continue;

            try {
                JsonNode root = mapper.readTree(json);

                for (int i = 0; i < 24; i++) {
                    rainSum[i] += root.get("rain").get(i).asDouble() * weight;
                    snowSum[i] += root.get("snowfall").get(i).asDouble() * weight;
                    tempSum[i] += root.get("temperature_2m").get(i).asDouble() * weight;
                    windSum[i] += root.get("wind_speed_10m").get(i).asDouble() * weight;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ArrayNode timeArray = mapper.createArrayNode();
        ArrayNode rainArray = mapper.createArrayNode();
        ArrayNode snowArray = mapper.createArrayNode();
        ArrayNode tempArray = mapper.createArrayNode();
        ArrayNode windArray = mapper.createArrayNode();

        for (int i = 0; i < 24; i++) {
            rainArray.add(rainSum[i] / weightSum);
            snowArray.add(snowSum[i] / weightSum);
            tempArray.add(tempSum[i] / weightSum);
            windArray.add(windSum[i] / weightSum);

            timeArray.add(targetDay + "T" + String.format("%02d:00", i));
        }

        ObjectNode result = mapper.createObjectNode();
        result.set("time", timeArray);
        result.set("rain", rainArray);
        result.set("snowfall", snowArray);
        result.set("temperature_2m", tempArray);
        result.set("wind_speed_10m", windArray);

        return result.toPrettyString();
    }

    // </editor-fold>
}
