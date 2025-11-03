package com.tourplanner.service;

import com.tourplanner.dto.BookingDTO;
import com.tourplanner.exception.BookingIdNotFoundException;
import com.tourplanner.model.Booking;
import com.tourplanner.repository.BookingRepository;
import com.tourplanner.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
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

    @Override
    public Mono<BookingDTO> createBooking(BookingDTO bookingDTO, String email) {
        Booking booking = mapToEntity(bookingDTO);

        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new Exception("User Not Found With Email: " + email)))
                .flatMap(user -> {
                    booking.setUserId(user.getUserId());
                    return bookingRepository.save(booking);
                }).map(this::mapToDto);
    }
}
