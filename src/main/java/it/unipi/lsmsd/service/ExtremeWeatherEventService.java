package it.unipi.lsmsd.service;

import it.unipi.lsmsd.DTO.ExtremeWeatherEventDTO;
import it.unipi.lsmsd.model.ExtremeWeatherEvent;
import it.unipi.lsmsd.repository.ExtremeWeatherEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExtremeWeatherEventService {
    @Autowired
    private ExtremeWeatherEventRepository eweRepository;

    // Insert new EWE
    public String addNewEWE(ExtremeWeatherEventDTO eweDTO) throws Exception{
        try{
            if (eweDTO == null)
                throw new IllegalArgumentException("ExtremeWeatherEvent is null: Check if request parameters are correct.");

            if (eweDTO.getCategory() == null)
                throw new IllegalArgumentException("Invalid ExtremeWeatherEvent: Missing required field 'category'.");

            if (eweDTO.getDateStart() == null)
                throw new IllegalArgumentException("Invalid ExtremeWeatherEvent: Missing required field 'dateStart'.");

            if (eweDTO.getDateEnd() == null)
                throw new IllegalArgumentException("Invalid ExtremeWeatherEvent: Missing required field 'dateEnd'.");

            if (eweDTO.getLongitude() == null)
                throw new IllegalArgumentException("Invalid ExtremeWeatherEvent: Missing required field 'longitude'.");

            if (eweDTO.getLatitude() == null)
                throw new IllegalArgumentException("Invalid ExtremeWeatherEvent: Missing required field 'latitude'.");

            if (eweDTO.getStrength() == null)
                throw new IllegalArgumentException("Invalid ExtremeWeatherEvent: Missing required field 'strength'.");

            if (eweDTO.getRadius() == null)
                throw new IllegalArgumentException("Invalid ExtremeWeatherEvent: Missing required field 'radius'.");

            ExtremeWeatherEvent eweEvent = new ExtremeWeatherEvent(eweDTO);

            eweRepository.save(eweEvent);
            return "Extreme Weather Event added successfully";
        }
        catch(Exception e){
            throw e;
        }
    }
}
