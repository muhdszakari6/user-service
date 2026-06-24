package com.example.userservice5.model.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String userId;
    private Boolean emailVerificationStatus;
}
