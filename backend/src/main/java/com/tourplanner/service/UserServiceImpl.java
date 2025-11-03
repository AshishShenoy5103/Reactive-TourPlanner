package com.tourplanner.service;

import com.tourplanner.dto.AdminProfileDTO;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class UserServiceImpl implements UserService{
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
        User newUser = new User();
        newUser.setEmail(userRegisterDTO.getEmail());
        newUser.setPasswordHash(passwordEncoder.encode(userRegisterDTO.getPassword()));

        return userRepository.save(newUser)
                .flatMap(savedUser -> {
                    Profile profile = new Profile();
                    profile.setUserId(savedUser.getUserId());
                    profile.setFirstName(userRegisterDTO.getFirstName());
                    profile.setLastName(userRegisterDTO.getLastName());
                    profile.setAadharNumber(userRegisterDTO.getAadharNumber());
                    profile.setCity(userRegisterDTO.getCity());
                    profile.setPhoneNumber(userRegisterDTO.getPhoneNumber());

                    return profileRepository.save(profile)
                            .thenReturn(userRegisterDTO);
                })
                .onErrorMap(DuplicateKeyException.class,e -> new RuntimeException("Email already registered"));
    }

    @Override
    public Mono<UserProfileDTO> getCurrentUserProfile(String email) {
        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found with email: " + email)))
                .flatMap(this::mapToUserProfileDTOReactive);
    }

    @Override
    public Flux<Booking> getAllBookingForAUser(String email) {
        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found with email: " + email)))
                .flatMapMany(user -> bookingRepository.findByUserId(user.getUserId()));
    }

    @Override
    public Mono<String> deleteUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new Exception("User not found with email: " + email)))
                .flatMap(user -> userRepository.delete(user))
                .then(Mono.just("User and related data deleted successfully for email: " + email));
    }


    @Override
    public Mono<Profile> updateCurrentUserProfile(String email, UpdateCurrentProfileInputDTO updateCurrentProfileInputDTO) {
        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new Exception("User Not Found With Email: " + email)))
                .flatMap(user ->
                        profileRepository.findByUserId(user.getUserId())
                                .switchIfEmpty(Mono.error(new Exception("Profile not found for user: " + email)))
                                .flatMap(profile -> {

                                    if(updateCurrentProfileInputDTO != null) {
                                        if(updateCurrentProfileInputDTO.getCity() != null) {
                                            profile.setCity(updateCurrentProfileInputDTO.getCity());
                                        }
                                        if(updateCurrentProfileInputDTO.getPhoneNumber() != null) {
                                            profile.setPhoneNumber(updateCurrentProfileInputDTO.getPhoneNumber());
                                        }
                                    }
                                    return profileRepository.save(profile);
                                })
                ).doOnError(Throwable::printStackTrace);
    }
}
