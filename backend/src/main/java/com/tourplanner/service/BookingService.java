package com.tourplanner.service;

import com.tourplanner.dto.BookingDTO;
import reactor.core.publisher.Mono;

public interface BookingService {
    Mono<BookingDTO> createBooking(BookingDTO bookingDTO, String email);
}
