package com.example.userservice5.service;

import com.example.userservice5.entity.BookingEntity;
import com.example.userservice5.entity.SessionEntity;
import com.example.userservice5.model.response.AvailableSessionResponse;
import com.example.userservice5.repository.BookingRepository;
import com.example.userservice5.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SessionService {
    private final SessionRepository sessionRepository;
    private final BookingRepository bookingRepository;

    public List<AvailableSessionResponse> getAvailableSessions(Long pitchId, LocalDate date) {
        ModelMapper modelMapper = new ModelMapper();
        List<SessionEntity> sessions = sessionRepository.findByPitchIdAndActiveTrue(pitchId);
        List<BookingEntity> bookings = bookingRepository.findBookingEntitiesByPitchAndDate(pitchId, date);
        Set<Long> bookedSessionIds = bookings.stream()
                .map(b -> b.getSession().getId())
                .collect(Collectors.toSet());

        List<AvailableSessionResponse> result = sessions.stream().map(sessionEntity -> {
            AvailableSessionResponse response = modelMapper.map(sessionEntity, AvailableSessionResponse.class);
            response.setStatus(bookedSessionIds.contains(sessionEntity.getId()) ? "BOOKED" : "AVAILABLE");
            return response;
        }).toList();

        return result;
    }
}
