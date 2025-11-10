package com.tourplanner.service;

import com.tourplanner.dto.AdminProfileDTO;
import com.tourplanner.dto.UserProfileDTO;
import com.tourplanner.exception.BookingIdNotFoundException;
import com.tourplanner.exception.UserNotFoundException;
import com.tourplanner.model.Booking;
import com.tourplanner.model.User;
import com.tourplanner.repository.BookingRepository;
import com.tourplanner.repository.ProfileRepository;
import com.tourplanner.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class AdminServiceImpl implements AdminService{
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private String maskEmail(String email) {
        if(email == null || !email.contains("@")) return "hidden";
        String[] parts = email.split("@");
        String name = parts[0];
        if(name.length() <= 2) return "***@" + parts[1];
        return name.charAt(0) + "***@" + parts[1];
    }

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
        String maskedEmail = maskEmail(email);
        log.info("Fetching admin profile for email: {}", maskedEmail);

        return userRepository.findByEmail(email)
                .doOnSubscribe(sub -> log.debug("Started searching admin user in DB for {}", maskedEmail))
                .doOnNext(user -> log.debug("Admin user found with ID: {}", user.getUserId()))

                // If no user found -> error
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("No admin user found for email: {}", maskedEmail);
                    return Mono.error(new UserNotFoundException("User not found with email: " + email));
                }))

                .flatMap(user -> {
                    log.debug("Mapping admin user to AdminProfileDTO for {}", maskedEmail);
                    return mapToAdminProfileDTOReactive(user)
                            .doOnSuccess(dto -> log.info("Successfully mapped admin profile for {} with ID={}", maskedEmail, user.getUserId()))
                            .doOnError(err -> log.error("Error mapping admin profile for {}: {}", maskedEmail, err.getMessage(), err));
                });
    }

    @Override
    public Mono<AdminProfileDTO> getUserById(Long userId) {
        log.info("Fetching user profile for userId: {}", userId);

        return userRepository.findById(userId)
                .doOnSubscribe(sub -> log.debug("Started searching user in DB for userId: {}", userId))
                .doOnNext(user -> log.debug("User found with ID: {} and type: {}", user.getUserId(), user.getUserType()))

                // If no user found -> error
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("No user found for userId: {}", userId);
                    return Mono.error(new UserNotFoundException("User not found with id: " + userId));
                }))

                .flatMap(user -> {
                    if ("ADMIN".equalsIgnoreCase(user.getUserType())) {
                        log.warn("User with ID {} is an admin, cannot fetch as regular user", userId);
                        return Mono.error(new UserNotFoundException("No user found with id: " + userId));
                    }
                    log.debug("Mapping user to AdminProfileDTO for userId: {}", userId);
                    return mapToAdminProfileDTOReactive(user)
                            .doOnSuccess(dto -> log.info("Successfully mapped user to AdminProfileDTO for userId: {}", userId))
                            .doOnError(err -> log.error("Error mapping user to AdminProfileDTO for userId {}: {}", userId, err.getMessage(), err));
                });
    }

    @Override
    public Mono<AdminProfileDTO> getAdminById(Long userId) {
        log.info("Fetching admin profile for userId: {}", userId);

        return userRepository.findById(userId)
                .doOnSubscribe(sub -> log.debug("Started searching user in DB for userId: {}", userId))
                .doOnNext(user -> log.debug("User found with ID: {} and type: {}", user.getUserId(), user.getUserType()))

                // If no user found -> error
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("No user found for userId: {}", userId);
                    return Mono.error(new UserNotFoundException("User not found with id: " + userId));
                }))

                .flatMap(user -> {
                    if ("USER".equalsIgnoreCase(user.getUserType())) {
                        log.warn("User with ID {} is a regular user, cannot fetch as admin", userId);
                        return Mono.error(new UserNotFoundException("No admin found with id: " + userId));
                    }
                    log.debug("Mapping admin user to AdminProfileDTO for userId: {}", userId);
                    return mapToAdminProfileDTOReactive(user)
                            .doOnSuccess(dto -> log.info("Successfully mapped admin profile for userId: {}", userId))
                            .doOnError(err -> log.error("Error mapping admin profile for userId {}: {}", userId, err.getMessage(), err));
                });
    }

    @Override
    public Mono<AdminProfileDTO> getUserByEmail(String email) {
        String maskedEmail = maskEmail(email);
        log.info("Fetching user profile for email: {}", maskedEmail);

        return userRepository.findByEmail(email)
                .doOnSubscribe(sub -> log.debug("Started searching user in DB for {}", maskedEmail))
                .doOnNext(user -> log.debug("User found with ID: {} and type: {}", user.getUserId(), user.getUserType()))

                // If no user found -> error
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("No user found for email: {}", maskedEmail);
                    return Mono.error(new UserNotFoundException("User not found with email: " + email));
                }))

                .flatMap(user -> {
                    if ("ADMIN".equalsIgnoreCase(user.getUserType())) {
                        log.warn("User with email {} is an admin, cannot fetch as regular user", maskedEmail);
                        return Mono.error(new UserNotFoundException("No user found with email: " + email));
                    }

                    log.debug("Mapping user to AdminProfileDTO for {}", maskedEmail);
                    return mapToAdminProfileDTOReactive(user)
                            .doOnSuccess(dto -> log.info("Successfully mapped user to AdminProfileDTO for {}", maskedEmail))
                            .doOnError(err -> log.error("Error mapping user to AdminProfileDTO for {}: {}", maskedEmail, err.getMessage(), err));
                });
    }

    @Override
    public Flux<AdminProfileDTO> getAllUser() {
        log.info("Fetching all users");

        return userRepository.findAll()
                .doOnSubscribe(sub -> log.debug("Started fetching all users from DB"))
                .doOnNext(user -> log.debug("User found with ID: {} and type: {}", user.getUserId(), user.getUserType()))

                .filter(user -> "USER".equalsIgnoreCase(user.getUserType()))

                .flatMap(user -> {
                    log.debug("Mapping user to AdminProfileDTO for userId: {}", user.getUserId());
                    return mapToAdminProfileDTOReactive(user)
                            .doOnSuccess(dto -> log.info("Successfully mapped user with ID: {}", user.getUserId()))
                            .doOnError(err -> log.error("Error mapping user with ID {}: {}", user.getUserId(), err.getMessage(), err));
                })
                .doOnComplete(() -> log.info("Completed fetching all users"))
                .doOnError(err -> log.error("Error fetching all users: {}", err.getMessage(), err));
    }

    @Override
    public Flux<AdminProfileDTO> getAllAdmin() {
        log.info("Fetching all users");

        return userRepository.findAll()
                .doOnSubscribe(sub -> log.debug("Started fetching all users from DB"))
                .doOnNext(user -> log.debug("User found with ID: {} and type: {}", user.getUserId(), user.getUserType()))

                .filter(user -> "ADMIN".equalsIgnoreCase(user.getUserType()))

                .flatMap(user -> {
                    log.debug("Mapping user to AdminProfileDTO for userId: {}", user.getUserId());
                    return mapToAdminProfileDTOReactive(user)
                            .doOnSuccess(dto -> log.info("Successfully mapped user with ID: {}", user.getUserId()))
                            .doOnError(err -> log.error("Error mapping user with ID {}: {}", user.getUserId(), err.getMessage(), err));
                })
                .doOnComplete(() -> log.info("Completed fetching all admins"))
                .doOnError(err -> log.error("Error fetching all admins: {}", err.getMessage(), err));
    }

    @Override
    public Mono<Booking> getBookingById(Long bookingId) {
        log.info("Fetching booking for bookingId: {}", bookingId);

        return bookingRepository.findById(bookingId)
                .doOnSubscribe(sub -> log.debug("Started searching booking in DB for bookingId: {}", bookingId))
                .doOnNext(booking -> log.debug("Booking found with ID: {} for userId: {}", booking.getBookingId(), booking.getUserId()))

                // No booking found -> error
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("No booking found for bookingId: {}", bookingId);
                    return Mono.error(new BookingIdNotFoundException("Booking not found with id: " + bookingId));
                }))
                .doOnError(err -> log.error("Error fetching booking for bookingId {}: {}", bookingId, err.getMessage(), err));
    }

    @Override
    public Flux<Booking> getAllBookings() {
        log.info("Fetching all bookings");

        return bookingRepository.findAll()
                .doOnSubscribe(sub -> log.debug("Started fetching all bookings from DB"))
                .doOnNext(booking -> log.debug("Booking found with ID: {} for userId: {}", booking.getBookingId(), booking.getUserId()))
                .doOnComplete(() -> log.info("Completed fetching all bookings"))
                .doOnError(err -> log.error("Error fetching all bookings: {}", err.getMessage(), err));
    }

    @Override
    public Mono<Booking> updateUserBooking(Long bookingId, String status) {
        log.info("Received request to update booking status for bookingId: {}", bookingId);

        return bookingRepository.findById(bookingId)
                .doOnSubscribe(sub -> log.debug("Started searching booking in DB for bookingId: {}", bookingId))
                .doOnNext(booking -> log.debug("Booking found with ID: {} for userId: {}", booking.getBookingId(), booking.getUserId()))

                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("No booking found for bookingId: {}", bookingId);
                    return Mono.error(new BookingIdNotFoundException("Booking not found with id: " + bookingId));
                }))

                .flatMap(booking -> {
                    log.debug("Updating status of bookingId {} to '{}'", bookingId, status);
                    booking.setStatus(status);
                    return bookingRepository.save(booking)
                            .doOnSuccess(updatedBooking -> log.info("Booking status updated successfully for bookingId {} to '{}'", bookingId, status))
                            .doOnError(err -> log.error("Error updating booking status for bookingId {}: {}", bookingId, err.getMessage(), err));
                })
                .doOnError(err -> log.error("Error in updateUserBooking for bookingId {}: {}", bookingId, err.getMessage(), err));
    }

    @Override
    public Mono<UserProfileDTO> updateUserById(Long userId, UserProfileDTO dto) {
        log.info("Received request to update user profile for userId: {}", userId);

        return userRepository.findById(userId)
                .doOnSubscribe(sub -> log.debug("Started searching user in DB for userId: {}", userId))
                .doOnNext(user -> log.debug("User found with ID: {} and email: {}", user.getUserId(), user.getEmail()))

                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("No user found for userId: {}", userId);
                    return Mono.error(new RuntimeException("User not found with id: " + userId));
                }))

                .flatMap(user -> {
                    if (dto.getEmail() != null) {
                        log.debug("Updating email of userId {} to '{}'", userId, dto.getEmail());
                        user.setEmail(dto.getEmail());
                    }

                    return userRepository.save(user)
                            .doOnSuccess(savedUser -> log.info("User email updated successfully for userId {}", userId))
                            .doOnError(err -> log.error("Error updating user email for userId {}: {}", userId, err.getMessage(), err))

                            .flatMap(savedUser -> {
                                return profileRepository.findByUserId(userId)
                                        .doOnSubscribe(sub -> log.debug("Started searching profile in DB for userId: {}", userId))
                                        .doOnNext(profile -> log.debug("Profile found for userId: {}", userId))
                                        .switchIfEmpty(Mono.defer(() -> {
                                            log.warn("No profile found for userId: {}", userId);
                                            return Mono.error(new RuntimeException("Profile not found for userId: " + userId));
                                        }))
                                        .flatMap(profile -> {
                                            log.debug("Updating profile details for userId {}", userId);
                                            if (dto.getFirstName() != null) profile.setFirstName(dto.getFirstName());
                                            if (dto.getLastName() != null) profile.setLastName(dto.getLastName());
                                            if (dto.getAadharNumber() != null) profile.setAadharNumber(dto.getAadharNumber());
                                            if (dto.getCity() != null) profile.setCity(dto.getCity());
                                            if (dto.getPhoneNumber() != null) profile.setPhoneNumber(dto.getPhoneNumber());

                                            return profileRepository.save(profile)
                                                    .doOnSuccess(savedProfile -> log.info("Profile updated successfully for userId {}", userId))
                                                    .doOnError(err -> log.error("Error updating profile for userId {}: {}", userId, err.getMessage(), err))
                                                    .map(savedProfile -> {
                                                        // Build DTO to return
                                                        UserProfileDTO updatedDTO = new UserProfileDTO();
                                                        updatedDTO.setEmail(savedUser.getEmail());
                                                        updatedDTO.setFirstName(savedProfile.getFirstName());
                                                        updatedDTO.setLastName(savedProfile.getLastName());
                                                        updatedDTO.setAadharNumber(savedProfile.getAadharNumber());
                                                        updatedDTO.setCity(savedProfile.getCity());
                                                        updatedDTO.setPhoneNumber(savedProfile.getPhoneNumber());
                                                        return updatedDTO;
                                                    });
                                        });
                            });
                })
                .doOnError(err -> log.error("Error in updateUserById for userId {}: {}", userId, err.getMessage(), err));
    }

    @Override
    public Mono<String> deleteUserById(Long userId) {
        log.info("Attempting to delete user and related data for {}", userId);

        return userRepository.findById(userId)
                .doOnSubscribe(sub -> log.debug("Started searching user in DB for {}", userId))
                .doOnNext(user -> log.debug("User found with ID: {}", user.getUserId()))

                // If no email found -> error
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("No user found with id: {}", userId);
                    return Mono.error(new UserNotFoundException("User not found with id: " + userId));
                }))

                .flatMap(user -> {
                    log.debug("Deleting user with ID: {}", user.getUserId());
                    return userRepository.delete(user)
                            .doOnSuccess(v -> log.info("User deleted successfully with ID={}", user.getUserId()))
                            .doOnError(err -> log.error("Error deleting user ID {}: {}", user.getUserId(), err.getMessage()));
                })

                .then(Mono.fromCallable(() -> {
                    log.info("User and related data deleted successfully for {}", userId);
                    return "User and related data deleted successfully for ID: " + userId;
                }));
    }
}
