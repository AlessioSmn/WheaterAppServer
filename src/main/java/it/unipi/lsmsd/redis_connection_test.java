/*
IMPORTANT: add the following dependency

<dependency>
     <groupId>redis.clients</groupId>
     <artifactId>jedis</artifactId>
     <version>4.3.0</version>
</dependency>
*/

package it.unipi.lsmsd;

import redis.clients.jedis.Jedis;

public class redis_connection_test {

    public static void main(String[] args) {

        // IMPORTANT: you must activate Redis before
        // After having it installed
        // Run 'redis-server' on the terminal, should start

        // Connection to Redis
        try (Jedis jedis = new Jedis("localhost", 6379)) {

            // Test setting a key-value pair in Redis
            jedis.set("string_key", "test value");

            // Retrieve the value associated with the key
            String value = jedis.get("string_key");

            // Check if the value matches what we set
            if ("test value".equals(value)) {
                System.out.println("Connection OK, Value retrieved: " + value);
            } else {
                System.out.println("Error: Value mismatch");
            }

        } catch (Exception e) {
            // Error message
            System.err.println("Connection error to Redis: " + e.getMessage());
        }
    }
}
