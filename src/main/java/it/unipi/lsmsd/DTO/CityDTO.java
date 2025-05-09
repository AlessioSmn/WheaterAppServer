package it.unipi.lsmsd.DTO;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.unipi.lsmsd.model.EWEThreshold;

@JsonIgnoreProperties(ignoreUnknown = true) // Ignore the properites that are not included in the DTO
public class CityDTO {
    // TODO: use id instead, used _id to match with Mongo for easier but recommended id to match City Model
    private String _id;
    private String name;
    private String region;
    private Double latitude;
    private Double longitude;
    private Double elevation;
    private EWEThreshold eweThresholds;
    
    // Json Write only properties - ignores when serializing to JSON
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String startDate; 
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String endDate;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Integer pastDays = 0; // Default Open-Meteo provides 0 past day forecast
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Integer forecastDays = 7; // Default Open-Meteo provides 7 day forecast
    
    // Setters and Getters

    public String get_id() {return _id;}
    public void set_id(String id) {this._id = id;}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    // @JsonGetter affects the serialized JSON (it controls what appears in the output).
    // @JsonSetter affects deserialization
    @JsonGetter("region") 
    public String getRegion() { return region; }
    //Necessary when mapping the JSON response from geocoding open-meteo 
    @JsonAlias({"region", "admin1"})
    public void setRegion(String region) { this.region = region; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public String getEndDate() { return endDate; }

    public Integer getPastDays() { return pastDays; }

    public void setPastDays(Integer pastHours) { this.pastDays = pastHours; }

    public Integer getForecastDays() { return forecastDays; }

    public void setForecastDays(Integer forecastHours) { this.forecastDays = forecastHours; }

    public Double getElevation() { return elevation; }
    public void setElevation(Double elevation) { this.elevation = elevation; }

    public EWEThreshold getEweThresholds() { return eweThresholds; }
    public void setEweThresholds(EWEThreshold eweThresholds) { this.eweThresholds = eweThresholds;}

    /**
     * Checks if the DTO has the necessary fields to construct the id (Name, region, latitude and longitude)
     * @return true if it has them, false otherwise
     */
    public boolean hasIdFields(){
        return getName() != null && !getName().isEmpty() &&
                getRegion() != null && !getRegion().isEmpty() &&
                getLatitude() != null &&
                getLongitude() != null;
    }
}
