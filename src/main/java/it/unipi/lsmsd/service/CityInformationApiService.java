package it.unipi.lsmsd.service;

import it.unipi.lsmsd.DTO.CityDTO;
import it.unipi.lsmsd.exception.CityNotFoundException;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class CityInformationApiService {

    private static final String BASE_URL  = "https://axqvoqvbfjpaamphztgd.functions.supabase.co/comuni?nome=";

    /**
     * Constructs a city DTO from a given city name. Only finds italian comuni.
     * @param cityName the request city name
     * @return a cityDTO of the constructed city, if found
     * @throws IllegalArgumentException on null or empty cityName field
     * @throws CityNotFoundException on city not found from the api
     * @throws IOException on connection error
     * @throws org.json.JSONException on JSON parsing errors
     * @apiNote API documentation: https://comuni-ita.readme.io/reference/comuni-1
     */
    public static CityDTO getCityInformation(String cityName) throws Exception{
        // First check on null city
        if (cityName == null || cityName.isBlank()) {
            throw new IllegalArgumentException("City name must be specified");
        }

        // Get the api response
        JSONArray responseJsonArray = getApiResponse(cityName);

        // If no city is found return an appropriate error
        if(responseJsonArray.isEmpty()){
            throw new CityNotFoundException("No city was found with the given name");
        }

        // Chose a city among the reported
        JSONObject foundCity;
        if(responseJsonArray.length() == 1) {
            foundCity = responseJsonArray.getJSONObject(0);
        }
        else {
            int chosenCityIndex = choseBestFit(cityName, responseJsonArray);
            foundCity = responseJsonArray.getJSONObject(chosenCityIndex);
        }

        // Construct the cityDto [NOTE: KEEP THE FIELDS IN ITALIAN]
        CityDTO cityDTO = new CityDTO();
        cityDTO.setName(foundCity.getString("nome"));
        cityDTO.setRegion(foundCity.getJSONObject("provincia").getString("regione"));
        cityDTO.setLatitude(foundCity.getJSONObject("coordinate").getDouble("lat"));
        cityDTO.setLongitude(foundCity.getJSONObject("coordinate").getDouble("lng"));
        // Note: the api doesn't provide any information on teh elevation, we have to get it from open meteo.

        // Return the DTO
        return cityDTO;
    }

    private static JSONArray getApiResponse(String cityName) throws IOException {
        // If there are initial or final spaces, remove them
        // If the city name has space in the middle (ex: Bagni di Lucca) put '+' in place of the spaces
        String sanitizedCityName = cityName.trim().replaceAll("\\s+", "+");

        // Concatenates the stringUrl with the city name
        String completeUrl = BASE_URL + sanitizedCityName;

        // Creates the HTTP connection
        URL url = new URL(completeUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");

        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            throw new RuntimeException("Failed request: " + responseCode);
        }

        // Read the response into the buffer
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = bufferedReader.readLine()) != null)
            response.append(inputLine);
        bufferedReader.close();

        // Parse the response
        JSONArray responseJsonArray = new JSONArray(response.toString());
        return responseJsonArray;
    }

    /**
     * Checks the api response for all teh found cities and returns
     * the index of the city with the most similar name to the requested name
     * @param requestedCityName requested City Name by the user
     * @param apiResponse array of cities
     * @return Index of the chosen city
     */
    private static int choseBestFit(String requestedCityName, JSONArray apiResponse){
        String requested = requestedCityName.trim().toLowerCase();
        int bestIndex = -1;
        int bestScore = Integer.MAX_VALUE;

        for (int i = 0; i < apiResponse.length(); i++) {
            JSONObject cityData = apiResponse.getJSONObject(i);

            // Get the city name
            String cityName = cityData.getString("nome").toLowerCase();
            System.out.println("Candidate name: " + cityName);

            // If an exact match is found, return immediately
            if (cityName.equals(requested)){
                System.out.println("Perfect match found");
                return i;
            }

            // Compute the score (smaller is better)
            int score = computeSimilarityScore(requested, cityName);

            if (score < bestScore) {
                bestScore = score;
                bestIndex = i;
            }
        }

        // Return the best fit
        return bestIndex;
    }


    /**
     * Calculates a basic similarity score between two strings. Lower means more similar.
     * Based on string length difference, supposes that the candidate contains the requested name.
     * @param requested requested city name
     * @param candidate found city name
     * @return score
     */
    private static int computeSimilarityScore(String requested, String candidate) {
        if (candidate.contains(requested)) {
            return Math.abs(candidate.length() - requested.length());
        } else {
            return Integer.MAX_VALUE - 1;
        }
    }
}
