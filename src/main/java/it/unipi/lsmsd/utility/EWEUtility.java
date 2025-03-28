package it.unipi.lsmsd.utility;

import it.unipi.lsmsd.model.EWEThreshold;
import it.unipi.lsmsd.model.ExtremeWeatherEventCategory;
import it.unipi.lsmsd.model.HourlyMeasurement;
import org.springframework.data.util.Pair;

import java.util.List;

public class EWEUtility {

    // TODO
    //  i put Integer to return also the strength, based on how distant the measurement is from the threshold
    //  Give me a feedback on whether you approve of this method and of this idea

    // TODO
    //  If you approve this then 1 has to be substituted with some logic to calculate the strength
    //  Also i put strength = 0 if no EWE is present, it may be easier to handle all of this if the dimension is always fixed to 5.
    // Returns and array with the found EWEs, given the measurements values passed
    // Each EWE is associated with its calculated strength
    public static List<Pair<ExtremeWeatherEventCategory, Integer>> getCurrentEWEs(HourlyMeasurement measurement, EWEThreshold thresholds){
        return List.of(
            Pair.of(ExtremeWeatherEventCategory.RAINSTORM, isRainstormOn(measurement.getRainfall(), thresholds.getRainfall())),
            Pair.of(ExtremeWeatherEventCategory.SNOWSTORM, isSnowstormOn(measurement.getSnowfall(), thresholds.getSnowfall())),
            Pair.of(ExtremeWeatherEventCategory.HURRICANE, isHurricaneOn(measurement.getWindSpeed(), thresholds.getWindSpeed())),
            Pair.of(ExtremeWeatherEventCategory.HEATWAVE, isHeatwaveOn(measurement.getTemperature(), thresholds.getMaxTemperature())),
            Pair.of(ExtremeWeatherEventCategory.COLDWAVE, isColdwaveOn(measurement.getTemperature(), thresholds.getMinTemperature()))
        );
    }

    public static List<QuadrupleEWEInformationHolder> getEmptyListOfEWEs(){
        return List.of(
            new QuadrupleEWEInformationHolder(ExtremeWeatherEventCategory.RAINSTORM, 0, null, null),
            new QuadrupleEWEInformationHolder(ExtremeWeatherEventCategory.SNOWSTORM, 0, null, null),
            new QuadrupleEWEInformationHolder(ExtremeWeatherEventCategory.HURRICANE, 0, null, null),
            new QuadrupleEWEInformationHolder(ExtremeWeatherEventCategory.HEATWAVE, 0, null, null),
            new QuadrupleEWEInformationHolder(ExtremeWeatherEventCategory.COLDWAVE, 0, null, null)
        );
    }

    public static int isRainstormOn(Double rainfall, Double rainfallThreshold) {
        if (rainfall >= rainfallThreshold) {
            // TODO
            //  add logic for the strength
            return 1;
        }
        return 0;
    }

    public static int isSnowstormOn(Double snowfall, Double snowfallThreshold) {
        if (snowfall >= snowfallThreshold) {
            // TODO
            //  add logic for the strength
            return 1;
        }
        return 0;
    }

    public static int isHurricaneOn(Double windSpeed, Double windSpeedThreshold) {
        if (windSpeed >= windSpeedThreshold) {
            // TODO
            //  add logic for the strength
            return 1;
        }
        return 0;
    }

    public static int isHeatwaveOn(Double temperature, Double maxTempThreshold) {
        if (temperature >= maxTempThreshold) {
            // TODO
            //  add logic for the strength
            return 1;
        }
        return 0;
    }

    public static int isColdwaveOn(Double temperature, Double minTempThreshold) {
        if (temperature <= minTempThreshold) {
            // TODO
            //  add logic for the strength
            return 1;
        }
        return 0;
    }

}
