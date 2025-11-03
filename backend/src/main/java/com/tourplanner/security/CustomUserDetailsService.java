package com.tourplanner.security;

import com.tourplanner.exception.UserNotFoundException;
import com.tourplanner.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

/*
    UserDetailsService is an interface which has loadUserByUsername method.
    We are overriding this loadUserByUsername(String username) and we are returning
    UserDetails User(email, password, ROLE_getUserType());

    Its for spring to verify that its a legitimate user later, now its just a User spring object.
*/

@Service
public class CustomUserDetailsService implements ReactiveUserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public Mono<UserDetails> findByUsername(String email) {
        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User Not Found")))
                .map(user -> new org.springframework.security.core.userdetails.User(
                        user.getEmail(),
                        user.getPasswordHash(),
                        List.of(new SimpleGrantedAuthority("ROLE_" + user.getUserType().toString()))
                ));
    }
}
