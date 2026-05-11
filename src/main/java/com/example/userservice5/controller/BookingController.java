package com.example.userservice5.controller;

import com.example.userservice5.dto.BookingDto;
import com.example.userservice5.model.request.CreateBookingRequest;
import com.example.userservice5.model.response.BookingResponse;
import com.example.userservice5.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("booking")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PostMapping()
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody CreateBookingRequest request){
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        BookingResponse bookingResponse = bookingService.createBooking(mapper.map(request, BookingDto.class));
        return ResponseEntity.status(201).body(bookingResponse);
    }
}
