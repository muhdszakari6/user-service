package com.example.userservice5.controller;

import com.example.userservice5.dto.BookingDto;
import com.example.userservice5.entity.BookingEntity;
import com.example.userservice5.enums.BookingStatus;
import com.example.userservice5.model.request.BookingRequest;
import com.example.userservice5.model.request.CreateBookingRequest;
import com.example.userservice5.model.response.BookingResponse;
import com.example.userservice5.model.response.GetPitchResponse;
import com.example.userservice5.model.response.OperationStatusModel;
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
            @RequestParam(required = false) Boolean deleted,
            @RequestParam(required = false) BookingStatus bookingStatus,
            Pageable pageable){
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        Page<BookingEntity> bookingEntities = bookingService.getBookings(userId, bookingDate, pitchId, pageable, deleted, bookingStatus);
        return bookingEntities.map(booking -> {
            BookingResponse response = mapper.map(booking, BookingResponse.class);
            response.setPitch(booking.getSession().getPitch().getName());
            response.setUserEmail(booking.getUser() != null ? booking.getUser().getEmail() : booking.getUserEmail());
            return response;
        });
    }

    @PreAuthorize("hasRole('PARTNER')")
    @GetMapping("/partners")
    public Page<BookingResponse> getPartnerBookings(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) LocalDate bookingDate,
            @RequestParam(required = false) Long pitchId,
            @RequestParam(required = false) Boolean deleted,
            Pageable pageable,
            @RequestParam(required = false) BookingStatus bookingStatus
            ){
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        Page<BookingEntity> bookingEntities = bookingService.getPartnerBookings(userId, bookingDate, pitchId, pageable, deleted, bookingStatus);
        return bookingEntities.map(booking -> {
            BookingResponse response = mapper.map(booking, BookingResponse.class);
            response.setPitch(booking.getSession().getPitch().getName());
            response.setUserEmail(booking.getUser() != null ? booking.getUser().getEmail() : booking.getUserEmail());
            return response;
        });
    }


    @PreAuthorize("hasRole('USER')")
    @GetMapping("/me")
    public Page<BookingResponse> getUserBookings(
            @RequestParam(required = false) LocalDate bookingDate,
            @RequestParam(required = false) Long pitchId,
            Pageable pageable,
            @RequestParam(required = false) BookingStatus bookingStatus
    ){
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        Page<BookingEntity> bookingEntities = bookingService.getUserBookings(bookingDate, pitchId, pageable, bookingStatus);
        return bookingEntities.map(booking -> {
            BookingResponse response = mapper.map(booking, BookingResponse.class);
            response.setPitch(booking.getSession().getPitch().getName());
            response.setUserEmail(booking.getUser() != null ? booking.getUser().getEmail() : booking.getUserEmail());
            return response;
        });
    }

    @PreAuthorize("hasAnyRole('PARTNER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<OperationStatusModel> deleteBooking(@PathVariable Long id){
        bookingService.deleteBooking(id);
        OperationStatusModel returnValue = new OperationStatusModel( "Successful", "Delete Booking");
        return ResponseEntity.status(200).body(returnValue);
    }

    @PreAuthorize("hasAnyRole('PARTNER')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<OperationStatusModel>  updateBookingStatus(@PathVariable Long id, @Valid @RequestBody BookingRequest bookingStatus){
        bookingService.updateStatus(id, bookingStatus);
        OperationStatusModel returnValue = new OperationStatusModel("Successful", "Update Booking Status");
        return ResponseEntity.status(200).body(returnValue);
    }



}
