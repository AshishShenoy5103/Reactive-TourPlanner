package com.tourplanner.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import com.tourplanner.model.User;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends ReactiveCrudRepository<User, Long> {
    Mono<User> findByEmail(String email);
}
