package it.unipi.lsmsd.service;

import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.unipi.lsmsd.repository.AnalyticsRepository;

@Service
public class AnalyticsService {

    @Autowired
    private AnalyticsRepository analyticsRepository;
    
    public List<Document> getAvgTemperaturePerCityLast30Days() {
        return analyticsRepository.getAvgTemperaturePerCityLast30Days();

    }

    public List<Document> getHottestDayPerCity() {
        return analyticsRepository.getHottestDayPerCity();
    }

    public List<Document> getTotalRainfallPerCityLast30Days(){
        return analyticsRepository.getTotalRainfallPerCityLast30Days();
    }
}
