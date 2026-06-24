package com.example.userservice5.controller;

import com.example.userservice5.dto.ResetPasswordDto;
import com.example.userservice5.dto.UserDto;
import com.example.userservice5.model.request.InitiateResetPasswordRequest;
import com.example.userservice5.model.request.ResetPasswordRequest;
import com.example.userservice5.model.request.UserSignupRequest;
import com.example.userservice5.model.response.OperationStatusModel;
import com.example.userservice5.model.response.ProfileResponse;
import com.example.userservice5.model.response.UserSignupResponse;
import com.example.userservice5.service.UserService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("users")
public class UserController {
    @Autowired
    UserService userService;
    @PostMapping(path = "/signup", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<UserSignupResponse> createUser(@RequestBody @Valid UserSignupRequest requestBody){
        ModelMapper mapper = new ModelMapper();
        UserDto dto = mapper.map(requestBody, UserDto.class);
        UserDto returnValue = userService.createUser(dto);
        return ResponseEntity.status(200).body(mapper.map(returnValue, UserSignupResponse.class));
    }

    @PostMapping("/partners/signup")
    public ResponseEntity<UserSignupResponse> partnerSignup(@RequestBody @Valid UserSignupRequest request) {
        ModelMapper mapper = new ModelMapper();
        UserDto dto = mapper.map(request, UserDto.class);
        UserDto returnValue = userService.createPartner(dto);
        return ResponseEntity.status(200).body(mapper.map(returnValue, UserSignupResponse.class));
    }

    @GetMapping(path = "/verify-email")
    public ResponseEntity<OperationStatusModel> verifyEmail(@RequestParam(name = "token") String token){
        OperationStatusModel returnValue = new OperationStatusModel( "Failed", "Email Verification");
        Boolean result = userService.verifyEmail(token);
        if(result){
            returnValue.setOperationName("Email Verification");
            returnValue.setOperationResult("Successful");
        }
        return ResponseEntity.status(200).body(returnValue);
    }

    @PostMapping(path = "/initiate-reset-password", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<OperationStatusModel> initiateResetPassword(@RequestBody @Valid InitiateResetPasswordRequest requestBody){
        OperationStatusModel returnValue = new OperationStatusModel( "Failed", "Initiate Password Reset Process");
        Boolean passwordResetInitiatedSuccessful = userService.initiateResetPassword(requestBody.getEmail());
        if(passwordResetInitiatedSuccessful){
            returnValue.setOperationResult("Successful");
        }
        return ResponseEntity.status(200).body(returnValue);
    }

    @PostMapping(path = "/reset-password", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<OperationStatusModel> resetPassword(@RequestBody @Valid ResetPasswordRequest requestBody){
        ModelMapper mapper = new ModelMapper();
        ResetPasswordDto dto = mapper.map(requestBody,ResetPasswordDto.class);
        OperationStatusModel returnValue = new OperationStatusModel( "Failed", "Reset Password");
        Boolean passwordResetSuccessful = userService.resetPassword(dto);
        if(passwordResetSuccessful){
            returnValue.setOperationResult("Successful");
        }
        return ResponseEntity.status(200).body(returnValue);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping(path = "")
    public ArrayList<String> getUsers(){
        return new ArrayList<>();
    }

    @GetMapping(path = "/me")
    public ResponseEntity<ProfileResponse> getCurrentUser(){
        ProfileResponse user = userService.getUser();
        return ResponseEntity.status(200).body(user);
    }
}
