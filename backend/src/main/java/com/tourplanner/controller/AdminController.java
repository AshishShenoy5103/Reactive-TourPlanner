package com.tourplanner.controller;

import com.tourplanner.dto.AdminProfileDTO;
import com.tourplanner.dto.UserProfileDTO;
import com.tourplanner.model.Booking;
import com.tourplanner.model.Profile;
import com.tourplanner.model.User;
import com.tourplanner.service.AdminService;
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

@Controller
public class AdminController {
    @Autowired
    private AdminService adminService;

    @QueryMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<AdminProfileDTO> getCurrentAdminProfile() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .flatMap(authentication -> {
                    String email = authentication.getName();
                    return adminService.getCurrentAdminProfile(email);
                });
    }

    @QueryMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<AdminProfileDTO> getUserById(@Argument Long userId) {
        return adminService.getUserById(userId);
    }

    @QueryMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<AdminProfileDTO> getAdminById(@Argument Long userId) {
        return adminService.getAdminById(userId);
    }

    @QueryMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<AdminProfileDTO> getUserByEmail(@Argument String email) {
        return adminService.getUserByEmail(email);
    }

    @QueryMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Flux<AdminProfileDTO> getAllUser() {
        return adminService.getAllUser();
    }

    @QueryMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<Booking> getBookingById(@Argument Long bookingId) {
        return adminService.getBookingById(bookingId);
    }

    @QueryMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Flux<Booking> getAllBookings() {
        return adminService.getAllBookings();
    }

    @MutationMapping
    public Mono<Booking> updateUserBooking(@Argument Long bookingId, @Argument String status) {
        return adminService.updateUserBooking(bookingId, status);
    }

}
