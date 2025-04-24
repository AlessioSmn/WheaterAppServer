package it.unipi.lsmsd.DTO;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

// DTO to save the Weather Measurement Response from Open-Meteo API for given city
@JsonInclude(JsonInclude.Include.NON_NULL) //prevent unnecessary null fields from serializing
public class HourlyMeasurementDTO {
    private String cityId; // MongoDB city ID after saving City
    private List<String> time; // ["2025-03-15T00:00", ...]

    @JsonProperty("temperature_2m")
    private List<Double> temperature;
    
    private List<Double> rain;
    private List<Double> snowfall;
    
    @JsonProperty("wind_speed_10m")
    private List<Double> windspeed;

    //Getters
    public String getCityId() { return cityId; }
    public List<String> getTime() { return time; }
    public List<Double> getTemperature() { return temperature; }
    public List<Double> getRain() { return rain; }
    public List<Double> getSnowfall() { return snowfall; }
    public List<Double> getWindspeed() { return windspeed; }

    //Setters
    public void setCityId(String city) { this.cityId = city; }
    public void setTime(List<String> time) { this.time = time; }
    public void setTemperature(List<Double> temperature) { this.temperature = temperature; }
    public void setRain(List<Double> rain) { this.rain = rain; }
    public void setSnowfall(List<Double> snowfall) { this.snowfall = snowfall; }
    public void setWindspeed(List<Double> windspeed) { this.windspeed = windspeed; }

}
