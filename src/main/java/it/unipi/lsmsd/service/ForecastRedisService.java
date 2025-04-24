package it.unipi.lsmsd.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.unipi.lsmsd.DTO.HourlyMeasurementDTO;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import java.io.IOException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ForecastRedisService {
    // Constant Jedis connection pool instance of Redis running on localhost and 
    // default Redis port 6379 to manage connections
    @Autowired
    private JedisPool jedisPool;
    private final ObjectMapper mapper = new ObjectMapper();

    //IMPORTANT NOTE : KEY structure --> forecast:pis-tus-43.7085-10.4036:2025-04-24

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
                String redisKey = "forecast:" + dto.getCityId() + ":" + day;
                String json = mapper.writeValueAsString(dayDTO);
    
                jedis.set(redisKey, json);                 // Save to Redis
                jedis.expire(redisKey, 86400);             // Set TTL: 24 hours
            }
        }
    }
    
    // Get 24hr forecast
    public String get24HrForecast(String cityId, String date) {
        String key = String.format("forecast:%s:%s", cityId, date);
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.get(key); // Can be returned directly to client
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
                String redisKey = "forecast:" + cityId + ":" + dayKey;  // Construct the Redis key using cityId and the date
    
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
    
}

/*
When scaling to 60–100 cities and updating hourly forecast data every 24 hours, there are some important factors to consider to keep latency low and optimize throughput when writing to Redis. Here's what you should think about:

🔁 1. Batching & Pipeline Writes
Problem: You're doing multiple SET + EXPIRE calls per city, which means 100s of round-trips to Redis.

Solution: Use Redis pipelining to batch all commands and send them together. This drastically reduces the number of network round-trips.

Example with Jedis pipelining:

java
Copy
Edit
Pipeline pipeline = jedis.pipelined();
for (...) {
    pipeline.set(redisKey, jsonValue);
    pipeline.expire(redisKey, 3600 * 24);
}
pipeline.sync(); // Executes all at once
⚙️ 2. Parallelizing the Processing
Problem: Processing 100 DTOs serially may increase latency.

Solution: Use parallel streams or a thread pool executor to handle multiple cities in parallel (especially the JSON mapping + Redis prep part).

Example:

java
Copy
Edit
cityDtos.parallelStream().forEach(dto -> saveForecast(dto));
Be cautious of Redis connection pool limits when doing this. If needed, tune JedisPool accordingly.

🚦 3. Redis Connection Pool Tuning
Problem: Too many concurrent Redis connections can exhaust the pool or cause blocking.

Solution: Configure the JedisPool:

Increase maxTotal, maxIdle

Set testOnBorrow = true for reliability

🧠 4. Payload Optimization
Store only what's necessary. Compressing JSON (e.g., shorter keys or using MessagePack) could help if payload size starts impacting Redis memory or network I/O.

📊 5. Monitoring & Metrics
Use Redis monitoring (INFO stats, MONITOR, etc.) to observe:

Command processing latency

Memory usage

Number of connections

Keyspace usage

Also, track the duration of your saveForecast() method for each city.

🧹 6. Avoiding Expiry Storm
If all 100 city forecasts expire at the same time, Redis may lag under load.

Solution: Add a randomized offset to the expiry time:

java
Copy
Edit
int ttl = 3600 * 24 + new Random().nextInt(300); // up to 5 min jitter
jedis.expire(redisKey, ttl);
⏱️ 7. Pre-compute Where Possible
If the data doesn’t change throughout the day, and reads dominate over writes, it's worth pre-computing the serialized JSON (e.g., outside the Redis loop), and even cache it in memory if reused.
 */