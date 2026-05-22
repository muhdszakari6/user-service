package com.example.userservice5.controller;

import com.example.userservice5.dto.BookingDto;
import com.example.userservice5.entity.BookingEntity;
import com.example.userservice5.model.request.CreateBookingRequest;
import com.example.userservice5.model.response.BookingResponse;
import com.example.userservice5.model.response.GetPitchResponse;
import com.example.userservice5.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

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

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping()
    public Page<BookingResponse> getBookings(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) LocalDate bookingDate,
            @RequestParam(required = false) Long pitchId,
            Pageable pageable){
        ModelMapper mapper = new ModelMapper();
        Page<BookingEntity> bookingEntities = bookingService.getBookings(userId, bookingDate, pitchId, pageable);
        return bookingEntities.map(booking -> {
            BookingResponse response = mapper.map(booking, BookingResponse.class);
            response.setPitch(booking.getSession().getPitch().getName());
            return response;
        });
    }

    @PreAuthorize("hasRole('PARTNER')")
    @GetMapping("/partners")
    public Page<BookingResponse> getPartnerBookings(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) LocalDate bookingDate,
            @RequestParam(required = false) Long pitchId,
            Pageable pageable
    ){
        ModelMapper mapper = new ModelMapper();
        Page<BookingEntity> bookingEntities = bookingService.getPartnerBookings(userId, bookingDate, pitchId, pageable);
        return bookingEntities.map(booking -> {
            BookingResponse response = mapper.map(booking, BookingResponse.class);
            response.setPitch(booking.getSession().getPitch().getName());
            return response;
        });
    }

}
