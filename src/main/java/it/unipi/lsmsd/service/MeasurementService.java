package it.unipi.lsmsd.service;

import it.unipi.lsmsd.model.HourlyMeasurement;
import it.unipi.lsmsd.repository.HourlyMeasurementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MeasurementService {

    @Autowired
    private HourlyMeasurementRepository measurementRepository;

    public List<HourlyMeasurement> getMeasurementsByCity(String cityName) {
        return measurementRepository.findByCityId(cityName);
    }
}
