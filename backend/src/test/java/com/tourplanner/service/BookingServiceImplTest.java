package com.tourplanner.service;

import com.tourplanner.dto.BookingDTO;
import com.tourplanner.exception.UserNotFoundException;
import com.tourplanner.main.MainApplication;
import com.tourplanner.model.Booking;
import com.tourplanner.model.User;
import com.tourplanner.repository.BookingRepository;
import com.tourplanner.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = MainApplication.class)
public class BookingServiceImplTest {
    @Autowired
    private BookingService bookingService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private BookingRepository bookingRepository;

    @Test
    void testCreateBookingSuccess() {
        BookingDTO bookingDTO = new BookingDTO();
        bookingDTO.setDestination("Goa");
        bookingDTO.setRate(18000);
        bookingDTO.setBookingDate(LocalDate.parse("2025-11-26"));
        bookingDTO.setNumberOfPeople(6);

        User user = new User();
        user.setUserId(1L);
        user.setEmail("test@gmail.com");

        Booking booking = new Booking();
        booking.setBookingId(1L);
        booking.setUserId(1L);
        booking.setDestination("Goa");
        booking.setRate(18000);
        booking.setBookingDate(LocalDate.parse("2025-11-26"));
        booking.setNumberOfPeople(6);

        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Mono.just(user));
        when(bookingRepository.save(any(Booking.class))).thenReturn(Mono.just(booking));

        Mono<BookingDTO> result = bookingService.createBooking(bookingDTO, "test@gmail.com");

        StepVerifier.create(result)
                .assertNext(savedBooking -> {
                    assertEquals("Goa", savedBooking.getDestination());
                    assertEquals(18000, savedBooking.getRate());
                    assertEquals("2025-11-26", savedBooking.getBookingDate().toString());
                    assertEquals(6, savedBooking.getNumberOfPeople());
                })
                .verifyComplete();

        verify(userRepository).findByEmail("test@gmail.com");
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void testCreateBookingFail() {
        BookingDTO bookingDTO = new BookingDTO();
        bookingDTO.setDestination("Goa");
        bookingDTO.setRate(18000);
        bookingDTO.setBookingDate(LocalDate.parse("2025-11-26"));
        bookingDTO.setNumberOfPeople(6);

        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Mono.empty());

        Mono<BookingDTO> result = bookingService.createBooking(bookingDTO, "test@gmail.com");

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof UserNotFoundException &&
                                throwable.getMessage().equals("User not found with email: test@gmail.com"))
                .verify();

        verify(userRepository).findByEmail("test@gmail.com");
    }
}
