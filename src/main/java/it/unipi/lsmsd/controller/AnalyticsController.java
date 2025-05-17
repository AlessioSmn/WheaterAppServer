package it.unipi.lsmsd.controller;

import it.unipi.lsmsd.model.ExtremeWeatherEventCategory;
import it.unipi.lsmsd.model.MeasurementField;
import it.unipi.lsmsd.service.AnalyticsService;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;


@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;


    // <editor-fold desc="Measurements analytics with single city as target [ measurement/ ]">

    @GetMapping("/measurement/count-for-city")
    public ResponseEntity<Object> getMeasurementCounts(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end
    ) {
        List<Document> serviceResponse = analyticsService.getMeasurementCountByCityInRange(start, end);
        return ResponseEntity.status(HttpStatus.OK).body(serviceResponse);
    }

    @GetMapping("/measurement/average-of-city")
    public ResponseEntity<Object> averageMeasurementInCityDuringPeriod(
            @RequestParam String cityId,
            @RequestParam MeasurementField measurementField,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate
    ) {
        List<Document> response = analyticsService.averageMeasurementInCityDuringPeriod(cityId, measurementField, startDate, endDate);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/measurement/average-per-month-of-city")
    public ResponseEntity<Object> averageMeasurementGroupByMonthInCityDuringPeriod(
            @RequestParam String cityId,
            @RequestParam MeasurementField measurementField,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate
    ) {
        List<Document> response = analyticsService.averageMeasurementGroupByMonthInCityDuringPeriod(cityId, measurementField, startDate, endDate);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/measurement/highest-of-city")
    public ResponseEntity<Object> highestMeasurementsInCityDuringPeriod(
            @RequestParam String cityId,
            @RequestParam MeasurementField measurementField,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate,
            @RequestParam int numMeasurementsToFind
    ) {
        List<Document> response = analyticsService.highestMeasurementsInCityDuringPeriod(cityId, measurementField, startDate, endDate, numMeasurementsToFind);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/measurement/lowest-of-city")
    public ResponseEntity<Object> lowestMeasurementsInCityDuringPeriod(
            @RequestParam String cityId,
            @RequestParam MeasurementField measurementField,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate,
            @RequestParam int numMeasurementsToFind
    ) {
        List<Document> response = analyticsService.lowestMeasurementsInCityDuringPeriod(cityId, measurementField, startDate, endDate, numMeasurementsToFind);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // </editor-fold>

    // <editor-fold desc="Measurements analytics across multiple cities [ measurement/ ]">

    @GetMapping("/measurement/average-highest-per-city")
    public ResponseEntity<Object> highestAverageMeasurementAcrossCities(
            @RequestParam MeasurementField measurementField,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate,
            @RequestParam int maxNumCitiesToFind
    ){
        List<Document> response = analyticsService.highestAverageMeasurementAcrossCities(measurementField, startDate, endDate, maxNumCitiesToFind);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @GetMapping("/measurement/average-lowest-per-city")
    public ResponseEntity<Object> lowestAverageMeasurementAcrossCities(
            @RequestParam MeasurementField measurementField,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate,
            @RequestParam int maxNumCitiesToFind
    ){
        List<Document> response = analyticsService.lowestAverageMeasurementAcrossCities(measurementField, startDate, endDate, maxNumCitiesToFind);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @GetMapping("/measurement/absolute-highest-per-city")
    public ResponseEntity<Object> highestMeasurementPerCity(
            @RequestParam MeasurementField measurementField,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate,
            @RequestParam int maxNumCitiesToFind
    ){
        List<Document> response = analyticsService.highestMeasurementPerCity(measurementField, startDate, endDate, maxNumCitiesToFind);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @GetMapping("/measurement/absolute-lowest-per-city")
    public ResponseEntity<Object> lowestMeasurementPerCity(
            @RequestParam MeasurementField measurementField,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate,
            @RequestParam int maxNumCitiesToFind
    ){
        List<Document> response = analyticsService.lowestMeasurementPerCity(measurementField, startDate, endDate, maxNumCitiesToFind);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    // </editor-fold>

    // <editor-fold desc="Measurements analytics of recent period [ measurement/recent/ ]">

    @GetMapping("/measurement/recent/average-per-city")
    public ResponseEntity<Object> averageMeasurementOfLastDaysAllCities(
            @RequestParam MeasurementField measurementField,
            @RequestParam int pastDays
    ) {
        List<Document> serviceResponse = analyticsService.getAverageMeasurementOfLastDaysAllCities(measurementField, pastDays);
        return ResponseEntity.status(HttpStatus.OK).body(serviceResponse);
    }

    @GetMapping("/measurement/recent/average-per-day-of-city")
    public ResponseEntity<Object> averageMeasurementPerDayOfCity(
            @RequestParam MeasurementField measurementField,
            @RequestParam String cityId,
            @RequestParam int pastDays
    ) {
        List<Document> serviceResponse = analyticsService.recentAverageMeasurementPerDayOfCity(measurementField, cityId, pastDays);
        return ResponseEntity.status(HttpStatus.OK).body(serviceResponse);
    }

    @GetMapping("/measurement/recent/total-per-city")
    public ResponseEntity<Object> totalMeasurementsOfLastDaysAllCities(
            @RequestParam MeasurementField measurementField,
            @RequestParam int pastDays
    ) {
        List<Document> serviceResponse = analyticsService.getTotalMeasurementLastDaysAllCities(measurementField, pastDays);
        return ResponseEntity.status(HttpStatus.OK).body(serviceResponse);
    }

    @GetMapping("/measurement/recent/total-per-day-of-city")
    public ResponseEntity<Object> totalMeasurementPerDayOfCity(
            @RequestParam MeasurementField measurementField,
            @RequestParam String cityId,
            @RequestParam int pastDays
    ) {
        List<Document> serviceResponse = analyticsService.recentTotalMeasurementPerDayOfCity(measurementField, cityId, pastDays);
        return ResponseEntity.status(HttpStatus.OK).body(serviceResponse);
    }

    // </editor-fold>



    // <editor-fold desc="Extreme Weather Event analytics across multiple cities [ ewe/ ]">

    @GetMapping("/ewe/affected-cities")
    public ResponseEntity<Object> citiesMostAffectedByEweInTimeRange(
            @RequestParam ExtremeWeatherEventCategory extremeWeatherEventCategory,
            @RequestParam int numCities,
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end
    ) {
        List<Document> serviceResponse = analyticsService.citiesMostAffectedByEweInTimeRange(numCities, extremeWeatherEventCategory, start, end);
        return ResponseEntity.status(HttpStatus.OK).body(serviceResponse);
    }

    @GetMapping("/ewe/count-of-at-least-strength-per-city")
    public ResponseEntity<Object> numberOfEweOfStrengthInTimeRange(
            @RequestParam ExtremeWeatherEventCategory extremeWeatherEventCategory,
            @RequestParam int minimumStrength,
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end
    ) {
        List<Document> serviceResponse = analyticsService.numberOfEweOfStrengthInTimeRange(minimumStrength, extremeWeatherEventCategory, start, end);
        return ResponseEntity.status(HttpStatus.OK).body(serviceResponse);
    }

    @GetMapping("/ewe/count-by-month-of-city")
    public ResponseEntity<Object> eweCountMonthlyAverage(
            @RequestParam String cityId,
            @RequestParam ExtremeWeatherEventCategory extremeWeatherEventCategory,
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end
    ){
        List<Document> serviceResponse = analyticsService.eweCountByMonth(cityId, extremeWeatherEventCategory, start, end);
        return ResponseEntity.status(HttpStatus.OK).body(serviceResponse);
    }

    @GetMapping("/ewe/strength-maximum-per-city")
    public ResponseEntity<Object> maximumEweStrengthInTimeRange(
            @RequestParam ExtremeWeatherEventCategory extremeWeatherEventCategory,
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end
    ) {
        List<Document> serviceResponse = analyticsService.maximumEweStrengthInTimeRange(extremeWeatherEventCategory, start, end);
        return ResponseEntity.status(HttpStatus.OK).body(serviceResponse);
    }

    @GetMapping("/ewe/strength-average-per-city")
    public ResponseEntity<Object> averageEweStrengthInTimeRange(
            @RequestParam ExtremeWeatherEventCategory extremeWeatherEventCategory,
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end
    ) {
        List<Document> serviceResponse = analyticsService.averageEweStrengthInTimeRange(extremeWeatherEventCategory, start, end);
        return ResponseEntity.status(HttpStatus.OK).body(serviceResponse);
    }

    @GetMapping("/ewe/duration-longest-per-city")
    public ResponseEntity<Object> longestEweDurationInTimeRange(
            @RequestParam ExtremeWeatherEventCategory extremeWeatherEventCategory,
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end
    ){
        List<Document> serviceResponse = analyticsService.longestDurationEweInTimeRange(extremeWeatherEventCategory, start, end);
        return ResponseEntity.status(HttpStatus.OK).body(serviceResponse);
    }

    @GetMapping("/ewe/duration-average-per-city")
    public ResponseEntity<Object> averageEweDurationInTimeRange(
            @RequestParam ExtremeWeatherEventCategory extremeWeatherEventCategory,
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end
    ){
        List<Document> serviceResponse = analyticsService.averageDurationEweInTimeRange(extremeWeatherEventCategory, start, end);
        return ResponseEntity.status(HttpStatus.OK).body(serviceResponse);
    }

    // </editor-fold>



    // <editor-fold desc="Information for client application">

    @GetMapping("/cities-information")
    public ResponseEntity<Object> citiesInformation() {
        List<Document> serviceResponse = analyticsService.citiesInformation();
        return ResponseEntity.status(HttpStatus.OK).body(serviceResponse);
    }

    // </editor-fold>
}
