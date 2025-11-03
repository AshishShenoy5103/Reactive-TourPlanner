package com.tourplanner.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;


/*
    This is user model it has
    userId, email, passwordHash, userType(USER, ADMIN), createdAt
*/

@Table("users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    private Long userId;

    @Column("email")
    private String email;

    @Column("password_hash")
    private String passwordHash;

    @Column("user_type")
    private String userType = UserType.USER.toString();

    @Column("created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}