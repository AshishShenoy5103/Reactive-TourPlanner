package com.tourplanner.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Table("bookings")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Booking {
    @Id
    private Long bookingId;

    @Column("user_id")
    private Long userId; // links to users.userId

    @Column("destination")
    private String destination;

    @Column("rate")
    private Integer rate;

    @Column("booking_date")
    private LocalDate bookingDate;

    @Column("number_of_people")
    private Integer numberOfPeople;

    @Column("created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column("status")
    private String status = "PENDING";
}
