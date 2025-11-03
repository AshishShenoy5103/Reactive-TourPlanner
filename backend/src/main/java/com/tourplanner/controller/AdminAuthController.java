package com.tourplanner.controller;

import com.tourplanner.exception.AdminAccessDeniedException;
import com.tourplanner.security.JwtGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

import java.util.Map;

@Controller
public class AdminAuthController {
    @Autowired
    private ReactiveAuthenticationManager reactiveAuthenticationManager;

    @Autowired
    private JwtGenerator jwtGenerator;

    @MutationMapping
    public Mono<Map<String, String>> loginAdmin(@Argument String email, @Argument String password) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(email, password);

        return reactiveAuthenticationManager.authenticate(authToken)
                .flatMap(authentication -> {
                    boolean isUser = authentication.getAuthorities().stream()
                            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

                    if(!isUser) {
                        return Mono.error(new AdminAccessDeniedException("Only Admins can login here"));
                    }

                    String token = jwtGenerator.generateToken(authentication, "ADMIN");
                    return Mono.just(Map.of("token", token));
                }).onErrorResume(e ->
                        Mono.just(Map.of("error", "Invalid email or password")));
    }
}
