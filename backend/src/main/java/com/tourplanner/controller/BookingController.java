package com.tourplanner.controller;

import com.tourplanner.dto.BookingDTO;
import com.tourplanner.repository.BookingRepository;
import com.tourplanner.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @MutationMapping
    @PreAuthorize("hasRole('USER')")
    public Mono<BookingDTO> createBooking(@Argument BookingDTO bookingDTO) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .flatMap(authentication -> {
                    String email = authentication.getName();
                    return bookingService.createBooking(bookingDTO, email);
                });
    }
}
