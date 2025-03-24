package it.unipi.lsmsd.service;

import it.unipi.lsmsd.DTO.APIResponseDTO;
import it.unipi.lsmsd.utility.Mapper;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import io.github.resilience4j.retry.annotation.Retry;
import com.fasterxml.jackson.core.JsonProcessingException;

// Hit the Open Meteo API to retrieve Weather Data
@Service
public class DataHarvestService {

    // Base Url of Open Meteo to retrive data from
    private final RestTemplate restTemplate;
    private static final String API_URL = "https://archive-api.open-meteo.com/v1/archive";

    public DataHarvestService() {
        //Configure timeouts by setting the request factory
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000); // Maximum time 5s to establish a connection with the server
        factory.setReadTimeout(5000); //Maximum time 5s to wait for a response after the connection is established
        this.restTemplate = new RestTemplate(factory);
    }

    // Get the weather data of the specific location for specific time frame
    @Retry(name = "OpenMeteoApiRetry")
    public APIResponseDTO getCityData(double latitude, double longitude, String startDate, String endDate) throws JsonProcessingException{
        // Append the parameters (Hourly Measurements of Temperature_2m, Rain, Snowfall and Wind_speed_10m) to the base URL
        String url = String.format("%s?latitude=%f&longitude=%f&start_date=%s&end_date=%s&hourly=temperature_2m,rain,snowfall,wind_speed_10m",
            API_URL, latitude, longitude, startDate, endDate);
    
        // API call and Response
        ResponseEntity<String> apiResponse = restTemplate.getForEntity(url, String.class);
        HttpStatusCode statusCode = apiResponse.getStatusCode();
        // Handle Unsucessful Requests
        if (apiResponse == null || apiResponse.getBody() == null || apiResponse.getBody().trim().isEmpty()) {
            throw new IllegalArgumentException("Open-Meteo API response is null or empty");
        } else if (statusCode.is5xxServerError()) {
            //Server error 500-599 to be retried (Handled by Resilence4j's config in application.yml)
            throw new HttpServerErrorException(statusCode, "Open-Meteo Server Error: " + apiResponse.getBody());
        } else if (statusCode.is4xxClientError()) {
            // No retry
            throw new HttpClientErrorException(statusCode, "Open-Meteo Client error: " + apiResponse.getBody());
        } else if (!statusCode.is2xxSuccessful()) {
            // No retry
            throw new RestClientException(
                "Open-Meteo Request failed, Status Code: " + statusCode.value() + ", Error: " + apiResponse.getBody());
        }

        // On success get the data and Map to the DTO
        APIResponseDTO responseDTO = Mapper.mapAPIResponse(apiResponse.getBody());
        return responseDTO;
    }
}