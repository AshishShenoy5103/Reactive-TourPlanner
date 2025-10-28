package com.tourplanner.controller;

import com.tourplanner.dto.UpdateCurrentProfileInputDTO;
import com.tourplanner.model.Profile;
import com.tourplanner.model.User;
import com.tourplanner.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
public class UserController {
    @Autowired
    private UserService userService;

    @QueryMapping
    public Mono<User> getUserByEmail(@Argument String email) {
        return userService.getCurrentUserProfile(email);
    }

    @QueryMapping
    public Mono<Profile> getProfileByUserId(@Argument Long userId) {
        return userService.getProfileByUserId(userId);
    }

    @MutationMapping
    public Mono<Profile> updateCurrentUserProfile(@Argument("email") String email, @Argument("input") UpdateCurrentProfileInputDTO updateCurrentProfileInputDTO) {
        return userService.updateCurrentUserProfile(email, updateCurrentProfileInputDTO);
    }

    @MutationMapping
    public Mono<String> deleteUserByEmail(@Argument String email) {
        return userService.deleteUserByEmail(email);
    }
}
