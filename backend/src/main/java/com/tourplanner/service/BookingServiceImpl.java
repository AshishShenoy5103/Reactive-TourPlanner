package com.tourplanner.service;

import com.tourplanner.dto.BookingDTO;
import com.tourplanner.exception.UserNotFoundException;
import com.tourplanner.model.Booking;
import com.tourplanner.repository.BookingRepository;
import com.tourplanner.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class BookingServiceImpl implements BookingService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private BookingDTO mapToDto(Booking booking) {
        BookingDTO dto = new BookingDTO();
        dto.setDestination(booking.getDestination());
        dto.setRate(booking.getRate());
        dto.setBookingDate(booking.getBookingDate());
        dto.setNumberOfPeople(booking.getNumberOfPeople());
        return dto;
    }

    private Booking mapToEntity(BookingDTO dto) {
        Booking booking = new Booking();
        booking.setDestination(dto.getDestination());
        booking.setRate(dto.getRate());
        booking.setBookingDate(dto.getBookingDate());
        booking.setNumberOfPeople(dto.getNumberOfPeople());
        return booking;
    }

    private String maskEmail(String email) {
        if(email == null || !email.contains("@")) return "hidden";
        String[] parts = email.split("@");
        String name = parts[0];
        if(name.length() <= 2) return "***@" + parts[1];
        return name.charAt(0) + "***@" + parts[1];
    }

    @Override
    public Mono<BookingDTO> createBooking(BookingDTO bookingDTO, String email) {
        String maskedEmail = maskEmail(email);
        log.info("Received request to create booking for user: {}", maskedEmail);

        Booking booking = mapToEntity(bookingDTO);

        return userRepository.findByEmail(email)
                .doOnSubscribe(sub -> log.debug("Started searching user in DB for {}", maskedEmail))
                .doOnNext(user -> log.debug("User found with ID: {}", user.getUserId()))

                // If no user found -> error
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("No user found for email: {}", maskedEmail);
                    return Mono.error(new UserNotFoundException("User not found with email: " + email));
                }))

                .flatMap(user -> {
                    log.debug("Setting userId {} for booking", user.getUserId());
                    booking.setUserId(user.getUserId());
                    return bookingRepository.save(booking)
                            .doOnSuccess(savedBooking -> log.info("Booking created successfully with ID: {}", savedBooking.getBookingId()))
                            .doOnError(err -> log.error("Error saving booking for {}: {}", maskedEmail, err.getMessage(), err));
                })

                .map(this::mapToDto)
                .doOnSuccess(dto -> log.info("Booking mapped to DTO successfully for {}", maskedEmail))
                .doOnError(err -> log.error("Error creating booking for {}: {}", maskedEmail, err.getMessage(), err));

    }
}
