package com.eventpass.backend.repository;

import com.eventpass.backend.entity.Event;
import com.eventpass.backend.entity.User;
import com.eventpass.backend.enums.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByStatus(EventStatus status);
    List<Event> findByOrganizer(User organizer);
}