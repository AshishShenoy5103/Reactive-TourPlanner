package com.tourplanner.repository;

import com.tourplanner.model.Profile;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ProfileRepository extends ReactiveCrudRepository<Profile, Long> {
    Mono<Profile> findByUserId(Long userId);
}
