package it.unipi.lsmsd.utility;

import it.unipi.lsmsd.DTO.HourlyMeasurementDTO;
import it.unipi.lsmsd.model.EWEThreshold;

import java.util.*;
import java.util.stream.Collectors;

public class StatisticsUtility {

    /**
     * Computes an EWEThreshold object from the given HourlyMeasurementDTO using symmetric percentiles.
     * It calculates the Xth and (100-X)th percentiles of the measurements and constructs a threshold:
     * high values are used for all parameters except for minTemperature, which uses the low percentile.
     *
     * @param dto The DTO containing hourly measurement data.
     * @param x   The percentile value to use for threshold computation (e.g., 10 means 10th and 90th).
     * @return An EWEThreshold instance populated with the computed percentile values.
     * @throws IllegalArgumentException if x is not in [0, 50] or if any measurement list is null or empty.
     */
    public static EWEThreshold getEweThresholdsFromMeasurements(HourlyMeasurementDTO dto, double x) {
        Map<String, Map<String, Double>> percentiles = computeSymmetricPercentiles(dto, x);
        return fromPercentiles(percentiles);
    }


    /**
     * Computes the Xth and (100-X)th percentiles for each numerical parameter in the given HourlyMeasurementDTO.
     *
     * @param dto The DTO containing hourly measurement data.
     * @param x   The desired percentile (e.g., 10 computes the 10th and 90th percentiles).
     * @return A map where each key is the parameter name (e.g., "temperature") and the value is another map
     *         with keys "low" and "high" representing the Xth and (100-X)th percentiles.
     * @throws IllegalArgumentException if x is not in [0, 50]
     */
    private static Map<String, Map<String, Double>> computeSymmetricPercentiles(HourlyMeasurementDTO dto, double x) {
        if (x < 0 || x > 50) {
            throw new IllegalArgumentException("Percentile value X must be between 0 and 50");
        }

        Map<String, Map<String, Double>> result = new LinkedHashMap<>();

        result.put("temperature", calculateSymmetricPercentiles(dto.getTemperature(), x));
        result.put("rain", calculateSymmetricPercentiles(dto.getRain(), x));
        result.put("snowfall", calculateSymmetricPercentiles(dto.getSnowfall(), x));
        result.put("windspeed", calculateSymmetricPercentiles(dto.getWindspeed(), x));

        return result;
    }

    private static Map<String, Double> calculateSymmetricPercentiles(List<Double> data, double x) {
        Map<String, Double> percentiles = new HashMap<>();

        if (data == null || data.isEmpty()) {
            percentiles.put("low", null);
            percentiles.put("high", null);
            return percentiles;
        }

        List<Double> sorted = data.stream()
                .filter(Objects::nonNull)
                .sorted()
                .collect(Collectors.toList());

        percentiles.put("low", getPercentile(sorted, x));
        percentiles.put("high", getPercentile(sorted, 100 - x));
        return percentiles;
    }

    private static Double getPercentile(List<Double> sortedData, double percentile) {
        if (sortedData.isEmpty()) return null;

        double index = percentile / 100.0 * (sortedData.size() - 1);
        int lowerIndex = (int) Math.floor(index);
        int upperIndex = (int) Math.ceil(index);

        if (lowerIndex == upperIndex) {
            return sortedData.get(lowerIndex);
        } else {
            double weight = index - lowerIndex;
            return sortedData.get(lowerIndex) * (1 - weight) + sortedData.get(upperIndex) * weight;
        }
    }


    /**
     * Creates an EWEThreshold instance using the specified percentile values.
     * Uses the high percentile for all parameters, except for minTemperature which uses the low percentile.
     *
     * @param percentiles A map where each key is a parameter name (e.g., "rain") and the value is another map
     *                    with keys "low" and "high" representing the percentiles.
     * @return A new instance of EWEThreshold populated with the selected percentiles.
     * @throws IllegalArgumentException if any required value is missing or null.
     */
    private static EWEThreshold fromPercentiles(Map<String, Map<String, Double>> percentiles) {
        double rainfall = getValue(percentiles, "rain", "high");
        double snowfall = getValue(percentiles, "snowfall", "high");
        double windSpeed = getValue(percentiles, "windspeed", "high");
        double maxTemperature = getValue(percentiles, "temperature", "high");
        double minTemperature = getValue(percentiles, "temperature", "low");

        return new EWEThreshold(rainfall, snowfall, maxTemperature, minTemperature, windSpeed);
    }

    private static double getValue(Map<String, Map<String, Double>> map, String parameter, String bound) {
        Map<String, Double> bounds = map.get(parameter);
        if (bounds == null || bounds.get(bound) == null) {
            throw new IllegalArgumentException("Missing percentile for " + parameter + " -> " + bound);
        }
        return bounds.get(bound);
    }
}
