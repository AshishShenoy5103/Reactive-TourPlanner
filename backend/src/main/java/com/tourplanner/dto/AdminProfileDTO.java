package com.tourplanner.dto;

import com.tourplanner.model.UserType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AdminProfileDTO {
    private Long userId;
    private String email;
    private String userType;
    private LocalDateTime createdAt;
    private String firstName;
    private String lastName;
    private String aadharNumber;
    private String city;
    private String phoneNumber;
}
