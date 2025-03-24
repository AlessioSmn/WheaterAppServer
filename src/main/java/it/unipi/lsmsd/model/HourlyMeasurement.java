package it.unipi.lsmsd.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document(collection = "hourly_measurements")
public class HourlyMeasurement {
    @Id
    private String id;
    private String cityId;
    private List<LocalDateTime> time;
    private List<Double> temperature;
    private List<Double> rainfall;
    private List<Double> snowfall;
    private List<Double> windSpeed;
    
    // Getters
    public String getId() { return id; }
    public String getCityId() { return cityId; }
    public List<LocalDateTime> getTime() { return time; }
    public List<Double> getTemperature() { return temperature; }
    public List<Double> getRainfall() { return rainfall; }
    public List<Double> getSnowfall() { return snowfall; }
    public List<Double> getWindSpeed() { return windSpeed; }
    
    //Setters
    public void setId(String id) { this.id = id; }
    public void setCityId(String cityId) { this.cityId = cityId; }
    public void setTime(List<LocalDateTime> time) { this.time = time; }
    public void setTemperature(List<Double> temperature) { this.temperature = temperature; }
    public void setRainfall(List<Double> rainfall) { this.rainfall = rainfall; }
    public void setSnowfall(List<Double> snowfall) { this.snowfall = snowfall; }
    public void setWindSpeed(List<Double> windSpeed) { this.windSpeed = windSpeed; }


    
    
}
