package com.example.userservice5.model.request;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class CreateBookingRequest {
    @NotNull()
    private Long sessionId;

    @NotNull()
    private LocalDate bookingDate;

    @Email(message = "Email is invalid")
    @Size(min = 3, message = "{validation.name.size.too_short}")
    @Size(max = 200, message = "{validation.name.size.too_long}")
    private String userEmail;

}