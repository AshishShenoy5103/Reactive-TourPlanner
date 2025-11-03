package com.tourplanner.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("profiles")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Profile {
    @Id
    private Long profileId;

    @Column("user_id")
    private Long userId; // links to users.userId

    @Column("first_name")
    private String firstName;

    @Column("last_name")
    private String lastName;

    @Column("aadhar_number")
    private String aadharNumber;

    @Column("city")
    private String city;

    @Column("phone_number")
    private String phoneNumber;
}
