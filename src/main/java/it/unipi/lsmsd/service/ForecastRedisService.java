package it.unipi.lsmsd.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.unipi.lsmsd.DTO.HourlyMeasurementDTO;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ForecastRedisService {
    // Constant Jedis connection pool instance of Redis running on localhost and 
    // default Redis port 6379 to manage connections
    @Autowired
    private JedisPool jedisPool;

    // Storing the forecast data in Redis per city per day as JSON
    // JSON promotes faster Read
    public void saveForecast(HourlyMeasurementDTO dto) throws JsonProcessingException{
        try (Jedis jedis = jedisPool.getResource()) {
    
            // 1. Extract data from DTO
            String cityId = dto.getCityId();
            List<String> timeList = dto.getTime();
            List<Double> tempList = dto.getTemperature();
            List<Double> rainList = dto.getRain();
            List<Double> snowList = dto.getSnowfall();
            List<Double> windList = dto.getWindspeed();
    
            // 2. Initialize a map to group hourly data by day
            Map<String, Map<String, List<Object>>> dayGroupedData = new HashMap<>();
    
            // 3. Loop through each hourly entry and group it under the correct day
            for (int i = 0; i < timeList.size(); i++) {
                String dateTime = timeList.get(i);
                String day = dateTime.split("T")[0]; // Extract just the date part (yyyy-MM-dd)
    
                // If day doesn't exist in map yet, initialize the inner map with empty lists
                dayGroupedData
                    .computeIfAbsent(day, k -> {
                        Map<String, List<Object>> m = new HashMap<>();
                        m.put("time", new ArrayList<>());
                        m.put("temperature", new ArrayList<>());
                        m.put("rain", new ArrayList<>());
                        m.put("snowfall", new ArrayList<>());
                        m.put("windspeed", new ArrayList<>());
                        return m;
                    });
    
                // 4. Add hourly values to the corresponding lists for the day
                Map<String, List<Object>> dayData = dayGroupedData.get(day);
                dayData.get("time").add(dateTime);
                dayData.get("temperature").add(tempList.get(i));
                dayData.get("rain").add(rainList.get(i));
                dayData.get("snowfall").add(snowList.get(i));
                dayData.get("windspeed").add(windList.get(i));
            }
    
            // 5. Initialize ObjectMapper to serialize daily data maps to JSON
            ObjectMapper mapper = new ObjectMapper();
    
            // 6. Save each day's data to Redis as a separate key-value pair
            for (Map.Entry<String, Map<String, List<Object>>> entry : dayGroupedData.entrySet()) {
                String day = entry.getKey();
                String redisKey = "forecast:" + cityId + ":" + day; // Key format: forecast:cityId:yyyy-MM-dd
                String jsonValue = mapper.writeValueAsString(entry.getValue()); // Serialize to JSON
    
                jedis.set(redisKey, jsonValue);         // Store in Redis
                jedis.expire(redisKey, 3600 * 24);       // Set TTL to 24 hours
            }
        } 
    }

    // Get 24hr forecast
    public String get24HrForecast(String city, String date) {
        String key = String.format("weather:%s:%s:24hr", city, date);
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.get(key);
        }
    }

    // Get full 7-day forecast
    public Map<String, String> get7DayForecast(String city, String date) {
        String key = String.format("weather:%s:%s:7day", city, date);
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hgetAll(key);
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