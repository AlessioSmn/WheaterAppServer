package it.unipi.lsmsd.controller;

import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.unipi.lsmsd.service.AnalyticsService;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

   @Autowired
    private AnalyticsService analyticsService;

    // Average Temperature per City for last 30 Days
    @GetMapping("/30days-avg-temperature")
    public List<Document> getAvgTemperatureLast30Days() {
        return analyticsService.getAvgTemperaturePerCityLast30Days();
    }
    
    // Average Temperature per City for last 30 Days
    @GetMapping("/hottest-day")
    public List<Document> getHottestDay() {
        return analyticsService.getHottestDayPerCity();
    }
    
    // Total Rainfall per City in Last 30 Days
    @GetMapping("/30days-total-rainfall")
    public List<Document> getTotalRainfall(){
        return analyticsService.getTotalRainfallPerCityLast30Days();
    }
    
}