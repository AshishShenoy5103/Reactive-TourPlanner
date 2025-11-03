package com.tourplanner.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileDTO {
    private String email;
    private String firstName;
    private String lastName;
    private String aadharNumber;
    private String city;
    private String phoneNumber;
}
