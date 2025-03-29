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

import java.util.Locale;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Hit the Open Meteo API to retrieve Weather Data
@Service
public class DataHarvestService {

    private static enum Type {
        OLD, RECENT
    }

    // Base Url of Open Meteo to retrive data from
    private final RestTemplate restTemplate;
    private static final String API_URL_OLD = "https://archive-api.open-meteo.com/v1/archive";
    private static final String API_URL_RECENT = "https://api.open-meteo.com/v1/forecast";

    //
    private static String getTimeMinusHours(int hours) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime timeAgo = now.minusHours(hours);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        return timeAgo.format(formatter);
    }

    private static String getTimePlusHours(int hours) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime timeAgo = now.plusHours(hours);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        return timeAgo.format(formatter);
    }

    public DataHarvestService() {
        //Configure timeouts by setting the request factory
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000); // Maximum time 5s to establish a connection with the server
        factory.setReadTimeout(5000); //Maximum time 5s to wait for a response after the connection is established
        this.restTemplate = new RestTemplate(factory);
    }

    // building the api URL
    private String buildCityDataUrl(double latitude, double longitude, String start, String end, Type type) {
        if (type == Type.OLD) {
            return String.format(Locale.US, "%s?latitude=%f&longitude=%f&start_date=%s&end_date=%s&hourly=temperature_2m,rain,snowfall,wind_speed_10m",
                    API_URL_OLD, latitude, longitude, start, end);
        }

        if (type == Type.RECENT) {
            return String.format(Locale.US, "%s?latitude=%f&longitude=%f&start_hour=%s&end_hour=%s&hourly=temperature_2m,rain,snowfall,wind_speed_10m",
                    API_URL_RECENT, latitude, longitude, start, end);
        }

        throw new IllegalArgumentException("Unknown type: " + type);
    }

    // Handle Unsucessful Requests
    private void checkAPIresponse(ResponseEntity<String> apiResponse) throws JsonProcessingException{
        HttpStatusCode statusCode = apiResponse.getStatusCode();

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
    }

    private APIResponseDTO callAPI(String url) throws JsonProcessingException {
        ResponseEntity<String> apiResponse = restTemplate.getForEntity(url, String.class);

        // Handle Unsucessful Requests
        checkAPIresponse(apiResponse);

        // On success get the data and Map to the DTO
        APIResponseDTO responseDTO = Mapper.mapAPIResponse(apiResponse.getBody());
        return responseDTO;
    }

    // Get the historical weather data of the specific location for specific time frame
    @Retry(name = "OpenMeteoApiRetry")
    public APIResponseDTO getCityHistoricalMeasurement(double latitude, double longitude, String startDate, String endDate) throws JsonProcessingException{
        // Append the parameters (Hourly Measurements of Temperature_2m, Rain, Snowfall and Wind_speed_10m) to the base URL
        String url = buildCityDataUrl(latitude, longitude, startDate, endDate, Type.OLD);
        // API call and Response
        return callAPI(url);
    }

    public APIResponseDTO getCityRecentMeasurementUsingHours (double latitude, double longitude, int pastHours, int forecastHours) throws JsonProcessingException {
        return getCityMeasurement(latitude, longitude, getTimeMinusHours(pastHours), getTimePlusHours(forecastHours));
    }

    @Retry(name="OpenMeteoApiRetry")
    public APIResponseDTO getCityMeasurement (double latitude, double longitude, String startHour, String endHour) throws JsonProcessingException {
        // create url for API
        String url = buildCityDataUrl(latitude, longitude, startHour, endHour, Type.RECENT);
        // API call and Response
        return callAPI(url);
    }
}