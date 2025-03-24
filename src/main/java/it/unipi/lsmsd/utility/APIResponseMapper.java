package it.unipi.lsmsd.utility;

import it.unipi.lsmsd.DTO.APIResponseDTO;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class APIResponseMapper {
    // Single reusable instance of objectMapper throughout the application's lifecycle
    private static final ObjectMapper objectMapper = new ObjectMapper();


    // Private constructor to prevent instantiation
    private APIResponseMapper() { }

    // Extracts hourly weather data from the JSON string into a list of MeasurementDTO
    public static APIResponseDTO mapAPIResponse(String json) throws JsonProcessingException{
        return objectMapper.readValue(json, APIResponseDTO.class);
    }
}
