package com.tourplanner.repository;

import com.tourplanner.model.Booking;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface BookingRepository extends ReactiveCrudRepository<Booking, Long> {
    Flux<Booking> findByUserId(Long userId);
}
