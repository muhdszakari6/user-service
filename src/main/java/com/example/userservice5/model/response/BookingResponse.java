package com.example.userservice5.model.response;
import com.example.userservice5.entity.PitchEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;


public class BookingResponse {
    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private String status;

    @Getter
    @Setter
    private LocalDate bookingDate;

    @Getter
    @Setter
    private SessionResponse session;

    @Getter
    @Setter
    private String pitch;

}
