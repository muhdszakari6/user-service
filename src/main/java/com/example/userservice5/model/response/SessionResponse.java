package com.example.userservice5.model.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

public class SessionResponse {
    @Getter
    @Setter
    private Long id;

    @Setter
    @Getter
    private String name;

    @Setter
    @Getter
    private LocalTime startTime;

    @Setter
    @Getter
    private LocalTime endTime;

    @Setter
    @Getter
    private Boolean active;

}
