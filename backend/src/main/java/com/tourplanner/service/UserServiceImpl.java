package com.tourplanner.service;

import com.tourplanner.dto.UpdateCurrentProfileInputDTO;
import com.tourplanner.model.Profile;
import com.tourplanner.model.User;
import com.tourplanner.repository.ProfileRepository;
import com.tourplanner.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UserServiceImpl implements UserService{
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Override
    public Mono<User> getCurrentUserProfile(String email) {
        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new Exception("User not found with email: " + email)));
    }

    @Override
    public Mono<String> deleteUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new Exception("User not found with email: " + email)))
                .flatMap(user -> userRepository.delete(user))
                .then(Mono.just("User and related data deleted successfully for email: " + email));
    }


    @Override
    public Mono<Profile> getProfileByUserId(Long userId) {
        return profileRepository.findByUserId(userId);
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
