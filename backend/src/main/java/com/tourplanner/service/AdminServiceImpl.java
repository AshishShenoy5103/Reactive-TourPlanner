package com.tourplanner.service;

import com.tourplanner.dto.AdminProfileDTO;
import com.tourplanner.exception.BookingIdNotFoundException;
import com.tourplanner.exception.UserNotFoundException;
import com.tourplanner.model.Booking;
import com.tourplanner.model.User;
import com.tourplanner.repository.BookingRepository;
import com.tourplanner.repository.ProfileRepository;
import com.tourplanner.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class AdminServiceImpl implements AdminService{
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private Mono<AdminProfileDTO> mapToAdminProfileDTOReactive(User user) {
        return profileRepository.findByUserId(user.getUserId())
                .map(profile -> new AdminProfileDTO(
                        user.getUserId(),
                        user.getEmail(),
                        user.getUserType(),
                        user.getCreatedAt(),
                        profile != null ? profile.getFirstName() : null,
                        profile != null ? profile.getLastName() : null,
                        profile != null ? profile.getAadharNumber() : null,
                        profile != null ? profile.getCity() : null,
                        profile != null ? profile.getPhoneNumber() : null
                ));
    }

    @Override
    public Mono<AdminProfileDTO> getCurrentAdminProfile(String email) {
        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found with email: " + email)))
                .flatMap(this::mapToAdminProfileDTOReactive);
    }

    @Override
    public Mono<AdminProfileDTO> getUserById(Long userId) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found with id: " + userId)))
                .flatMap(user -> {
                    if ("ADMIN".equalsIgnoreCase(user.getUserType())) {
                        return Mono.error(new UserNotFoundException("No user found with id: " + userId));
                    }
                    return mapToAdminProfileDTOReactive(user);
                });
    }

    @Override
    public Mono<AdminProfileDTO> getAdminById(Long userId) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found with id: " + userId)))
                .flatMap(user -> {
                    if ("USER".equalsIgnoreCase(user.getUserType())) {
                        return Mono.error(new UserNotFoundException("No user found with id: " + userId));
                    }
                    return mapToAdminProfileDTOReactive(user);
                });
    }

    @Override
    public Mono<AdminProfileDTO> getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found with email: " + email)))
                .flatMap(user -> {
                    if ("ADMIN".equalsIgnoreCase(user.getUserType())) {
                        return Mono.error(new UserNotFoundException("No user found with email: " + email));
                    }

                    return mapToAdminProfileDTOReactive(user);
                });
    }

    @Override
    public Flux<AdminProfileDTO> getAllUser() {
        return userRepository.findAll()
                .filter(user -> "USER".equalsIgnoreCase(user.getUserType()))
                .flatMap(this::mapToAdminProfileDTOReactive);
    }

    @Override
    public Mono<Booking> getBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .switchIfEmpty(Mono.error(new BookingIdNotFoundException("Booking not found with id: " + bookingId)));
    }

    @Override
    public Flux<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Override
    public Mono<Booking> updateUserBooking(Long bookingId, String status) {
        return bookingRepository.findById(bookingId)
                .switchIfEmpty(Mono.error(new BookingIdNotFoundException("Booking not found with id: " + bookingId)))
                .flatMap(booking -> {
                    booking.setStatus(status);
                    return bookingRepository.save(booking);
                });
    }
}
