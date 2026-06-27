package com.example.userservice5.service;

import com.example.userservice5.dto.BookingDto;
import com.example.userservice5.entity.BookingEntity;
import com.example.userservice5.entity.PitchEntity;
import com.example.userservice5.entity.SessionEntity;
import com.example.userservice5.entity.UserEntity;
import com.example.userservice5.enums.BookingStatus;
import com.example.userservice5.exception.ApiException;
import com.example.userservice5.model.request.BookingRequest;
import com.example.userservice5.model.response.BookingResponse;
import com.example.userservice5.model.response.GetPitchResponse;
import com.example.userservice5.repository.BookingRepository;
import com.example.userservice5.repository.SessionRepository;
import com.example.userservice5.repository.UserRepository;
import com.example.userservice5.security.UserPrincipal;
import jakarta.validation.Valid;
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
import java.time.LocalDateTime;
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
        } else if (bookingDto.getUserEmail() == null || bookingDto.getUserEmail().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Please provide a valid email");
        }

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        SessionEntity session = sessionRepository.findById(bookingDto.getSessionId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Session Not Found"));

        Optional<BookingEntity> bookingEntity = bookingRepository.findBySessionAndBookingDateAndDeletedAtIsNullAndStatus(session, bookingDto.getBookingDate(), BookingStatus.SUCCESSFUL);

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
            throw new ApiException(HttpStatus.CONFLICT, " Slot already booked");
        }
        PitchEntity pitch = savedBooking.getSession().getPitch();
        BookingResponse returnValue = modelMapper.map(savedBooking, BookingResponse.class);
        returnValue.setPitch(pitch.getName());
        return returnValue;
    }

    public Page<BookingEntity> getBookings(Long userId, LocalDate bookingDate, Long pitchId, Pageable pageable, Boolean deleted, BookingStatus bookingStatus) {
        return bookingRepository.findBookings(userId, bookingDate, pitchId, deleted, bookingStatus, pageable);
    }

    public Page<BookingEntity> getPartnerBookings(Long userId, LocalDate bookingDate, Long pitchId, Pageable pageable, Boolean deleted, BookingStatus bookingStatus) {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long partnerId = userPrincipal.getUserEntity().getId();
        return bookingRepository.findPartnerBookings(userId, bookingDate, pitchId, pageable, partnerId, deleted, bookingStatus);
    }


    public Page<BookingEntity> getUserBookings(LocalDate bookingDate, Long pitchId, Pageable pageable, BookingStatus bookingStatus) {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userPrincipal.getUserEntity().getId();
        return bookingRepository.findUserBookings( userId, bookingDate, pitchId, bookingStatus, pageable);
    }

    public void deleteBooking(Long id) {
        BookingEntity bookingEntity = validateBooking(id);
        bookingEntity.setDeletedAt(LocalDateTime.now());
        bookingRepository.save(bookingEntity);
    }


    public void updateStatus(Long id, BookingRequest bookingStatus) {
        BookingEntity bookingEntity = validateBooking(id);
        bookingEntity.setStatus(bookingStatus.getStatus());
        bookingRepository.save(bookingEntity);
    }

    private BookingEntity validateBooking(Long id) {
        BookingEntity bookingEntity = bookingRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Booking Not Found"));
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long partnerId = userPrincipal.getUserEntity().getId();
        if (!bookingEntity.getSession().getPitch().getUserDetail().getId().equals(partnerId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can't perform this operation");
        }
        return bookingEntity;
    }

}
