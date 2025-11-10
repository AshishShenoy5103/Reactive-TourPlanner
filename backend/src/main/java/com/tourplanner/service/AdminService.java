package com.tourplanner.service;

import com.tourplanner.dto.AdminProfileDTO;
import com.tourplanner.dto.UserProfileDTO;
import com.tourplanner.model.Booking;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AdminService {
    Mono<AdminProfileDTO> getCurrentAdminProfile(String email);
    Mono<AdminProfileDTO> getUserById(Long userId);
    Mono<AdminProfileDTO> getAdminById(Long userId);
    Mono<AdminProfileDTO> getUserByEmail(String email);
    Flux<AdminProfileDTO> getAllUser();
    Flux<AdminProfileDTO> getAllAdmin();
    Mono<Booking> getBookingById(Long bookingId);
    Flux<Booking> getAllBookings();
    Mono<Booking> updateUserBooking(Long bookingId, String email);
    Mono<UserProfileDTO> updateUserById(Long userId, UserProfileDTO userProfileDTO);
    Mono<String> deleteUserById(Long userId);
}
