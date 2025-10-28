package com.tourplanner.service;

import com.tourplanner.dto.UpdateCurrentProfileInputDTO;
import com.tourplanner.model.Profile;
import com.tourplanner.model.User;
import reactor.core.publisher.Mono;

public interface UserService {
    Mono<User> getCurrentUserProfile(String email);
    Mono<String> deleteUserByEmail(String email);
    Mono<Profile> getProfileByUserId(Long userId);
    Mono<Profile> updateCurrentUserProfile(String email, UpdateCurrentProfileInputDTO updateCurrentProfileInputDTO);
}
