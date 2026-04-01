package com.xpensetrack.repository;

import com.xpensetrack.model.UpcomingEvent;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.LocalDate;
import java.util.List;

public interface UpcomingEventRepository extends MongoRepository<UpcomingEvent, String> {
    List<UpcomingEvent> findByUserIdAndDueDateGreaterThanEqualOrderByDueDateAsc(String userId, LocalDate date);
}
