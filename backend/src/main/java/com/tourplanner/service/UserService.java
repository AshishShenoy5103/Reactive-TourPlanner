package com.tourplanner.service;

import com.tourplanner.dto.UpdateCurrentProfileInputDTO;
import com.tourplanner.dto.UserProfileDTO;
import com.tourplanner.dto.UserRegisterDTO;
import com.tourplanner.model.Booking;
import com.tourplanner.model.Profile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserService {
    Mono<UserRegisterDTO> registerUser(UserRegisterDTO userRegisterDTO);
    Mono<UserProfileDTO> getCurrentUserProfile(String email);
    Flux<Booking> getAllBookingForAUser(String email);
    Mono<Profile> updateCurrentUserProfile(String email, UpdateCurrentProfileInputDTO updateCurrentProfileInputDTO);
    Mono<String> deleteUserByEmail(String email);
}
