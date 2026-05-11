package com.example.userservice5.dto;

import com.example.userservice5.entity.UserEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

public class BookingDto {
    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private Long sessionId;

    @Getter
    @Setter
    private LocalDate bookingDate;

    @Getter
    @Setter
    private String userEmail;

    @Getter
    @Setter
    private UserEntity user;

    @Getter
    @Setter
    private SessionDto session;

}
