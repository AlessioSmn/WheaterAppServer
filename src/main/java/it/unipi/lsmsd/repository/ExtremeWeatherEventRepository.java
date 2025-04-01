package it.unipi.lsmsd.repository;

import it.unipi.lsmsd.model.ExtremeWeatherEventCategory;
import org.springframework.data.mongodb.repository.MongoRepository;
import it.unipi.lsmsd.model.ExtremeWeatherEvent;

import java.time.LocalDateTime;
import java.util.List;

public interface ExtremeWeatherEventRepository extends MongoRepository<ExtremeWeatherEvent, String> {
    List<ExtremeWeatherEvent> findByCityIdAndDateEndIsNull(String cityId);
    List<ExtremeWeatherEvent> findByCityIdAndCategoryAndDateStartBetweenOrderByDateStart(
            String cityId,
            ExtremeWeatherEventCategory category,
            LocalDateTime startTime,
            LocalDateTime endTime
    );
}
