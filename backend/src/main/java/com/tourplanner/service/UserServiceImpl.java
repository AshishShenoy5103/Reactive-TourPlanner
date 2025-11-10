package com.tourplanner.service;

import com.tourplanner.dto.UpdateCurrentProfileInputDTO;
import com.tourplanner.dto.UserProfileDTO;
import com.tourplanner.dto.UserRegisterDTO;
import com.tourplanner.exception.UserNotFoundException;
import com.tourplanner.model.Booking;
import com.tourplanner.model.Profile;
import com.tourplanner.model.User;
import com.tourplanner.repository.BookingRepository;
import com.tourplanner.repository.ProfileRepository;
import com.tourplanner.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

// log.info(), log.error(), log.debug()

@Service
@Slf4j
public class UserServiceImpl implements UserService{
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String maskEmail(String email) {
        if(email == null || !email.contains("@")) return "hidden";
        String[] parts = email.split("@");
        String name = parts[0];
        if(name.length() <= 2) return "***@" + parts[1];
        return name.charAt(0) + "***@" + parts[1];
    }

    private Mono<UserProfileDTO> mapToUserProfileDTOReactive(User user) {
        return profileRepository.findByUserId(user.getUserId())
                .map(profile -> new UserProfileDTO(
                        user.getEmail(),
                        profile != null ? profile.getFirstName() : null,
                        profile != null ? profile.getLastName() : null,
                        profile != null ? profile.getAadharNumber() : null,
                        profile != null ? profile.getCity() : null,
                        profile != null ? profile.getPhoneNumber() : null
                ));
    }

    @Override
    public Mono<UserRegisterDTO> registerUser(UserRegisterDTO userRegisterDTO) {
        String maskedEmail = maskEmail(userRegisterDTO.getEmail());
        log.info("Received request to register new user: {}", maskedEmail);

        User newUser = new User();
        newUser.setEmail(userRegisterDTO.getEmail());
        newUser.setPasswordHash(passwordEncoder.encode(userRegisterDTO.getPassword()));

        return userRepository.save(newUser)
                .doOnSubscribe(sub -> log.debug("Attempting to save new user: {}", maskedEmail))
                .doOnNext(savedUser -> log.debug("User saved successfully with ID: {}", savedUser.getUserId()))

                .flatMap(savedUser -> {
                    log.debug("Creating profile for userId: {}", savedUser.getUserId());

                    Profile profile = new Profile();
                    profile.setUserId(savedUser.getUserId());
                    profile.setFirstName(userRegisterDTO.getFirstName());
                    profile.setLastName(userRegisterDTO.getLastName());
                    profile.setAadharNumber(userRegisterDTO.getAadharNumber());
                    profile.setCity(userRegisterDTO.getCity());
                    profile.setPhoneNumber(userRegisterDTO.getPhoneNumber());

                    return profileRepository.save(profile)
                            .doOnSuccess(savedProfile -> log.info("Profile created successfully for {} with ID={}", maskedEmail, savedProfile.getUserId()))
                            .doOnError(err -> log.error("Error saving profile for {}: {}", maskedEmail, err.getMessage(), err))
                            .thenReturn(userRegisterDTO)
                            .doOnSuccess(dto -> log.info("User registration completed successfully for {} with ID={}", maskedEmail, savedUser.getUserId()));
                })

                .doOnError(err -> log.error("Error during registration for {}: {}", maskedEmail, err.getMessage(), err))

                // If an email present -> error
                .onErrorMap(DuplicateKeyException.class, e -> {
                    log.warn("Registration failed: Email {} already registered", maskedEmail);
                    return new RuntimeException("Email already registered");
                });
    }

    @Override
    public Mono<UserProfileDTO> getCurrentUserProfile(String email) {
        String maskedEmail = maskEmail(email);
        log.info("Fetching user profile for email: {}", maskedEmail);

        return userRepository.findByEmail(email)
                .doOnSubscribe(sub -> log.debug("Started searching user in DB for {}", maskedEmail))
                .doOnNext(user -> log.debug("User found with ID: {}", user.getUserId()))

                // If no email found -> error
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("No user found for email: {}", maskedEmail);
                    return Mono.error(new UserNotFoundException("User not found with email: " + email));
                }))

                .flatMap(user -> {
                    log.debug("Mapping user to UserProfileDTO for {}", maskedEmail);
                    return mapToUserProfileDTOReactive(user)
                            .doOnSuccess(dto -> log.info("Successfully mapped profile for {} with ID={}", maskedEmail, user.getUserId()))
                            .doOnError(err -> log.error("Error mapping user profile for {}: {}", maskedEmail, err.getMessage()));
                });
    }

    @Override
    public Flux<Booking> getAllBookingForAUser(String email) {
        String maskedEmail = maskEmail(email);
        log.info("Fetching all bookings for user: {}", maskedEmail);

        return userRepository.findByEmail(email)
                .doOnSubscribe(sub -> log.debug("Started searching user in DB for {}", maskedEmail))
                .doOnNext(user -> log.debug("User found with ID: {}", user.getUserId()))

                // If no email found -> error
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("No user found for email: {}", maskedEmail);
                    return Mono.error(new UserNotFoundException("User not found with email: " + email));
                }))

                .flatMapMany(user -> {
                    log.debug("Fetching bookings for userId: {}", user.getUserId());
                    return bookingRepository.findByUserId(user.getUserId())
                            .doOnSubscribe(sub -> log.debug("Started querying bookings for {}", maskedEmail))
                            .doOnNext(booking -> log.debug("Found booking with ID: {}", booking.getBookingId()))
                            .doOnComplete(() -> log.info("Completed fetching bookings for {} with ID={}", maskedEmail, user.getUserId()))
                            .doOnError(err -> log.error("Error fetching bookings for {}: {}", maskedEmail, err.getMessage()));
                });
    }

    @Override
    public Mono<String> deleteUserByEmail(String email) {
        String maskedEmail = maskEmail(email);
        log.info("Attempting to delete user and related data for {}", maskedEmail);

        return userRepository.findByEmail(email)
                .doOnSubscribe(sub -> log.debug("Started searching user in DB for {}", maskedEmail))
                .doOnNext(user -> log.debug("User found with ID: {}", user.getUserId()))

                // If no email found -> error
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("No user found for email: {}", maskedEmail);
                    return Mono.error(new UserNotFoundException("User not found with email: " + email));
                }))

                .flatMap(user -> {
                    log.debug("Deleting user with ID: {}", user.getUserId());
                    return userRepository.delete(user)
                            .doOnSuccess(v -> log.info("User deleted successfully for {} with ID={}", maskedEmail, user.getUserId()))
                            .doOnError(err -> log.error("Error deleting user for {}: {}", maskedEmail, err.getMessage()));
                })

                .then(Mono.fromCallable(() -> {
                    log.info("User and related data deleted successfully for {}", maskedEmail);
                    return "User and related data deleted successfully for email: " + email;
                }));
    }

    @Override
    public Mono<Profile> updateCurrentUserProfile(String email, UpdateCurrentProfileInputDTO updateCurrentProfileInputDTO) {
        String maskedEmail = maskEmail(email);
        log.info("Received request to update profile for user: {}", maskedEmail);

        return userRepository.findByEmail(email)
                .doOnSubscribe(sub -> log.debug("Started searching user in DB for {}", maskedEmail))
                .doOnNext(user -> log.debug("User found with ID: {}", user.getUserId()))

                // If no user found -> error
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("No user found for email: {}", maskedEmail);
                    return Mono.error(new UserNotFoundException("User not found with email: " + email));
                }))

                // Found user -> find profile
                .flatMap(user -> profileRepository.findByUserId(user.getUserId())
                        .doOnSubscribe(sub -> log.debug("Started searching profile for userId: {}", user.getUserId()))
                        .doOnNext(profile -> log.debug("Profile found with ID: {}", profile.getProfileId()))

                        // If no profile found -> error
                        .switchIfEmpty(Mono.defer(() -> {
                            log.warn("No profile found for user: {}", maskedEmail);
                            return Mono.error(new RuntimeException("Profile not found for user: " + email));
                        }))

                        // Update fields and save
                        .flatMap(profile -> {
                            log.info("Updating profile for user: {}", maskedEmail);

                            if (updateCurrentProfileInputDTO != null) {
                                if (updateCurrentProfileInputDTO.getCity() != null) {
                                    log.debug("Updating city to: {}", updateCurrentProfileInputDTO.getCity());
                                    profile.setCity(updateCurrentProfileInputDTO.getCity());
                                }
                                if (updateCurrentProfileInputDTO.getPhoneNumber() != null) {
                                    log.debug("Updating phone number to: {}", updateCurrentProfileInputDTO.getPhoneNumber());
                                    profile.setPhoneNumber(updateCurrentProfileInputDTO.getPhoneNumber());
                                }
                            }

                            return profileRepository.save(profile)
                                    .doOnSuccess(updated -> log.info("Successfully updated profile for {} with userID={}", maskedEmail, profile.getUserId()))
                                    .doOnError(err -> log.error("Error saving profile for {}: {}", maskedEmail, err.getMessage(), err));
                        })
                )

                // Top-level error logging (catches errors from any upstream operator)
                .doOnError(err -> log.error("Error updating profile for {}: {}", maskedEmail, err.getMessage(), err));
    }
}
