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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
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

    public void refreshForecastAutomaticFromOpenMeteo(String cityId) throws JsonProcessingException{

        Optional<City> optionalCity = cityRepository.findById(cityId);
        if(optionalCity.isEmpty()){
            return;
        }
        City city = optionalCity.get();

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

    // function to calcuate distance between 2 cities using lat e long
    public static double haversine(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    /*
     * IMPORTANT NOTE : 
     *  KEY structure --> forecast:{bucket-i}:pis-tus-43.7085-10.4036:2025-04-24
     */

    private String retrieve24HrForecast(String name, String region, double lat, double lon, String date){
        String cityId = CityUtility.generateCityId(name, region , lat, lon);
        String dateDto = date;
        // CityDTO.startDate is optional so check for null value
        // LocalDate.now converted to UTC+0 timezone since data are stored in UTC+0
        LocalDate utcDate = ZonedDateTime.now(ZoneOffset.UTC).toLocalDate();
        String targetDate = dateDto != null ? dateDto : utcDate.toString();

        String redisKey = String.format("forecast:{%s}:%s:%s", CityBucketResolver.getBucket(cityId), cityId, targetDate);

        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.get(redisKey); // Can be returned directly to client
        }
    }
    
    // Storing the forecast data in Redis as daily-split JSON for each city
    // JSON promotes faster Read
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
    
    // Get 24hr forecast
    public String get24HrForecast(CityDTO cityDTO) {
        return retrieve24HrForecast(cityDTO.getName(), cityDTO.getRegion(), cityDTO.getLatitude(), cityDTO.getLongitude(), cityDTO.getStartDate());
    }

    // Get full 7-day forecast
    public String get7DayForecast(CityDTO cityDTO) throws IOException {
        String cityId = CityUtility.generateCityId(cityDTO.getName(), cityDTO.getRegion() , cityDTO.getLatitude(), cityDTO.getLongitude());

        try (Jedis jedis = jedisPool.getResource()) {  
            //Define date format for Redis key (yyyy-MM-dd) and get current date
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd"); 
            LocalDate currentDate = LocalDate.now();
            
            // List to hold the 7-day forecast data
            List<HourlyMeasurementDTO> allDaysData = new ArrayList<>();
    
            //Loop through the next 7 days (including today)
            for (int i = 0; i < 7; i++) {
                String dayKey = currentDate.plusDays(i).format(formatter);  // Format the date for each day (e.g., "2025-03-15")
                String redisKey = String.format("forecast:{%s}:%s", cityId, dayKey); // Construct the Redis key using cityId and the date

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

    public String deleteAllForecast() {
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
            return "Deleted All Forecast Data";
        }
    }

    // a very basic tool for estimate forecast for any arbitrary city
    // a weighted avg is used, where the weights are the distance between the target city and the others
    // the other cities are the cities stored in mongo in the same region of the target
    public String get24HrForecastArbCity(CityDTO cityDTO) {
        String region = cityDTO.getRegion();
        String redisRegionKey = "region:" + region;

        double lat = cityDTO.getLatitude();
        double lon = cityDTO.getLongitude();

        List<City> targetCities = new ArrayList<>();
        int count = 0;
        try (Jedis jedis = jedisPool.getResource()) {
            Set<String> cityKeys = jedis.smembers(redisRegionKey);

            for (String cityKey : cityKeys) {
                Map<String, String> cityHash = jedis.hgetAll(cityKey);

                if (!cityHash.isEmpty()) {
                    City city = new City();
                    city.setName(cityHash.get("name"));
                    city.setRegion(cityHash.get("region"));
                    String[] parts = cityKey.split("-");
                    city.setLatitude(Double.parseDouble(parts[2]));
                    city.setLongitude(Double.parseDouble(parts[3]));
                    targetCities.add(city);
                    ++count;
                }
            }
        }
        String date = cityDTO.getStartDate(); // format "YYYY-MM-DD"
        if (date == null || date.isBlank()) {
            date = LocalDate.now().toString();
        }

        double[] rainSum = new double[24];
        double[] snowSum = new double[24];
        double[] tempSum = new double[24];
        double[] windSum = new double[24];
        double weightSum = 0.0;

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode distancesArray = mapper.createArrayNode();

        for (City city : targetCities) {
            double distance = haversine(lat, lon, city.getLatitude(), city.getLongitude());
            double weight = distance == 0 ? 1000 : 1000 / distance;
            weightSum += weight;

            ObjectNode cityDistance = mapper.createObjectNode();
            cityDistance.put("city", city.getName());
            cityDistance.put("latitude", city.getLatitude());
            cityDistance.put("longitude", city.getLongitude());
            cityDistance.put("distance_km", distance);
            cityDistance.put("weight", weight);
            distancesArray.add(cityDistance);

            String json = retrieve24HrForecast(city.getName(), city.getRegion(), city.getLatitude(), city.getLongitude(), date);
            if (json == null || json.isEmpty()) continue;
            ++count;

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

            timeArray.add(date + "T" + String.format("%02d:00", i));
        }

        ObjectNode result = mapper.createObjectNode();
        result.set("time", timeArray);
        result.set("rain", rainArray);
        result.set("snowfall", snowArray);
        result.set("temperature_2m", tempArray);
        result.set("wind_speed_10m", windArray);

        return result.toPrettyString() + "\ncount: " + count;
    }
}
