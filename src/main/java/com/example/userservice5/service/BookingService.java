package com.example.userservice5.service;

import com.example.userservice5.dto.BookingDto;
import com.example.userservice5.entity.BookingEntity;
import com.example.userservice5.entity.PitchEntity;
import com.example.userservice5.entity.SessionEntity;
import com.example.userservice5.entity.UserEntity;
import com.example.userservice5.enums.BookingStatus;
import com.example.userservice5.exception.ApiException;
import com.example.userservice5.model.response.BookingResponse;
import com.example.userservice5.model.response.GetPitchResponse;
import com.example.userservice5.repository.BookingRepository;
import com.example.userservice5.repository.SessionRepository;
import com.example.userservice5.repository.UserRepository;
import com.example.userservice5.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.modelmapper.spi.MatchingStrategy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;


    @Transactional
    public BookingResponse createBooking(BookingDto bookingDto){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getPrincipal() instanceof UserPrincipal) {
            // logged-in user logic
            UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            UserEntity existingUser = userPrincipal.getUserEntity();
            UserEntity managedUser = userRepository.findByEmail(existingUser.getEmail());
            if(managedUser != null){
                bookingDto.setUser(managedUser);
                bookingDto.setUserEmail(managedUser.getEmail());
            }
        }

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        SessionEntity session = sessionRepository.findById(bookingDto.getSessionId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Session Not Found"));

        Optional<BookingEntity> bookingEntity = bookingRepository.findBySessionAndBookingDateAndDeletedAtIsNull(session, bookingDto.getBookingDate());

        if(bookingEntity.isPresent()){
            throw new ApiException(HttpStatus.BAD_REQUEST, "Already Booked");
        }

        BookingEntity booking = modelMapper.map(bookingDto, BookingEntity.class);
        booking.setSession(session);
        booking.setStatus(BookingStatus.PENDING);


        BookingEntity savedBooking;
        try {
            savedBooking = bookingRepository.save(booking);
        } catch (DataIntegrityViolationException e) {
            throw new ApiException(HttpStatus.CONFLICT, "Slot already booked");
        }
        PitchEntity pitch = savedBooking.getSession().getPitch();
        BookingResponse returnValue = modelMapper.map(savedBooking, BookingResponse.class);
        returnValue.setPitch(pitch.getName());
        return returnValue;
    }

    public Page<BookingEntity> getBookings(Long userId, LocalDate bookingDate, Long pitchId, Pageable pageable) {
        return bookingRepository.findBookings(userId, bookingDate, pitchId, pageable);
    }

    public Page<BookingEntity> getPartnerBookings(Long userId, LocalDate bookingDate, Long pitchId, Pageable pageable) {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long partnerId = userPrincipal.getUserEntity().getId();
        return bookingRepository.findPartnerBookings(userId, bookingDate, pitchId, pageable, partnerId);
    }


}
