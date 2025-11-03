package com.tourplanner.controller;

import com.tourplanner.dto.UpdateCurrentProfileInputDTO;
import com.tourplanner.dto.UserProfileDTO;
import com.tourplanner.model.Booking;
import com.tourplanner.model.Profile;
import com.tourplanner.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/*
    Each HTTP request runs in its own thread, so spring stores in HttpServletRequest object (thread-local storage).
    In Reactive theres no fixed thread per request. So ThreadLocal and HttpServletRequest dont work as intended.
*/


@Controller
public class UserController {
    @Autowired
    private UserService userService;

    @QueryMapping
    @PreAuthorize("hasRole('USER')")
    public Mono<UserProfileDTO> getCurrentUserProfile() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .flatMap(authentication -> {
                   String email = authentication.getName();
                   return userService.getCurrentUserProfile(email);
                });
    }

    @QueryMapping
    @PreAuthorize("hasRole('USER')")
    public Flux<Booking> getAllBookingForAUser() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .flatMapMany(authentication -> {
                    String email = authentication.getName();
                    return userService.getAllBookingForAUser(email);
                });
    }

    @MutationMapping
    @PreAuthorize("hasRole('USER')")
    public Mono<Profile> updateCurrentUserProfile(@Argument("email") String email, @Argument("input") UpdateCurrentProfileInputDTO updateCurrentProfileInputDTO) {
        return userService.updateCurrentUserProfile(email, updateCurrentProfileInputDTO);
    }

    @MutationMapping
    @PreAuthorize("hasRole('USER')")
    public Mono<String> deleteUserByEmail(@Argument String email) {
        return userService.deleteUserByEmail(email);
    }
}
