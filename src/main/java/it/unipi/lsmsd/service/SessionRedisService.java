package it.unipi.lsmsd.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Service
public class SessionRedisService {
    
    // Constant Jedis connection pool instance of Redis running on localhost and 
    // default Redis port 6379 to manage connections
    @Autowired
    private JedisPool jedisPool;

    // Saves a user session in Redis
    public void saveSession(String token, String username) {
        // Retrieves a Jedis connection from the pool
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.setex(token, 3600, username); // 1-hour expiration
        }
    }
    // Retrieves the username form given session token
    public String getUsernameFromSession(String token) {
        try (Jedis jedis = jedisPool.getResource()) { 
            return jedis.get(token);
        }
    }
    // Deletes the session when the user logs out
    public void deleteSession(String token) {
        try (Jedis jedis = jedisPool.getResource()) { 
            jedis.del(token);
        }
    }
}