package com.example.userservice5.model.response;

import org.springframework.http.HttpStatus;

import java.util.Date;

public class ApiError {
    private String message;
    private HttpStatus status;
    private Date timeStamp;

    public ApiError(){

    }
    public ApiError(String message, HttpStatus status, Date timeStamp) {
        this.message = message;
        this.status = status;
        this.timeStamp = timeStamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public static class BookingResponse {
    }
}
