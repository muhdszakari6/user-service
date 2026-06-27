package com.example.userservice5.service;
import com.example.userservice5.dto.PitchDto;
import com.example.userservice5.dto.SessionConfigurationDto;
import com.example.userservice5.dto.SessionDto;
import com.example.userservice5.entity.PitchEntity;
import com.example.userservice5.entity.SessionEntity;
import com.example.userservice5.entity.UserEntity;
import com.example.userservice5.enums.CreationMode;
import com.example.userservice5.exception.ApiException;
import com.example.userservice5.repository.BookingRepository;
import com.example.userservice5.repository.PitchRepository;
import com.example.userservice5.repository.SessionConfigurationRepository;
import com.example.userservice5.repository.SessionRepository;
import com.example.userservice5.repository.UserRepository;
import com.example.userservice5.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.html.Option;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class PitchService {
    private final PitchRepository pitchRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    @Transactional()
    public PitchDto createPitch(PitchDto requestBody) {
        PitchEntity existingPitch = pitchRepository.findByNameAndDeletedAtIsNull(requestBody.getName());
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity existingUser = userPrincipal.getUserEntity();
        UserEntity managedUser = userRepository.findByEmail(existingUser.getEmail());

        if(managedUser == null){
            throw new ApiException(HttpStatus.BAD_REQUEST, "Something went wrong");
        }

        if(existingPitch != null){
            throw new ApiException(HttpStatus.BAD_REQUEST, "A pitch with this name exists already");
        }

        SessionConfigurationDto sessionConfigurationDto = this.createSessionConfiguration(requestBody);
        requestBody.setSessionConfiguration(sessionConfigurationDto);
        ModelMapper mapper = new ModelMapper();

        PitchEntity pitch = mapper.map(requestBody, PitchEntity.class);
        pitch.setUserDetail(managedUser);
        if (pitch.getSessions() != null) {
            pitch.getSessions().forEach(session -> session.setActive(true));
        }

        PitchEntity storedPitch = pitchRepository.save(pitch);

        return mapper.map(storedPitch, PitchDto.class);
    }

    @Transactional()
    public PitchDto updatePitch(PitchDto requestBody, Long id) {
        Optional<PitchEntity> existingPitch = pitchRepository.findByIdAndDeletedAtIsNull(id);
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity existingUser = userPrincipal.getUserEntity();
        if (existingPitch.isEmpty()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Pitch does not exist");
        }
        if (!existingUser.getUserId().equals(existingPitch.get().getUserDetail().getUserId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You don't own this pitch");
        }

        PitchEntity pitch = pitchRepository.findByNameAndDeletedAtIsNull(requestBody.getName());
        if(pitch != null && !pitch.getId().equals(id)){
            throw new ApiException(HttpStatus.BAD_REQUEST, "A pitch with this name exists already");
        }

        ModelMapper modelMapper = new ModelMapper();
        PitchEntity pitchEntity = existingPitch.get();
        pitchEntity.setName(requestBody.getName());
        pitchEntity.setType(requestBody.getType());
        pitchEntity.setLocation(requestBody.getLocation());

        Collection<SessionEntity> existingSessions =  pitchEntity.getSessions();
        if (existingSessions == null) {
            existingSessions = new ArrayList<>();
        }

        List<SessionDto> sessions =  requestBody.getSessions();

        for(SessionDto sessionDto : sessions){
            if(sessionDto.getId() != null) {
               Optional<SessionEntity> existingSession = existingSessions.stream().filter(
                        session -> session.getId().equals(sessionDto.getId())
                ).findFirst();

               if(existingSession.isPresent()){
                   SessionEntity foundSession = existingSession.get();
                   foundSession.setName(sessionDto.getName());
                   foundSession.setStartTime(sessionDto.getStartTime());
                   foundSession.setEndTime(sessionDto.getEndTime());
                   foundSession.setActive(sessionDto.getActive());
               }
            }else {
                SessionEntity sessionEntity = modelMapper.map(sessionDto, SessionEntity.class);
                sessionEntity.setActive(true);
                pitchEntity.addSession(sessionEntity);
            }
        }

        List<Long> requestSessionIds = sessions.stream()
                .map(SessionDto::getId)
                .filter(sessionId -> sessionId!=null)
                .toList();

        List<SessionEntity> sessionsToRemove = existingSessions.stream()
                .filter(existingSession -> existingSession.getId() != null)
                .filter(session -> !requestSessionIds.contains(session.getId())).toList();

        sessionsToRemove.forEach(session -> {
            if (!bookingRepository.existsBySessionAndDeletedAtIsNull(session)) {
                pitchEntity.removeSession(session);
            }
        });

        PitchEntity updatedPitch = pitchRepository.save(pitchEntity);

        return modelMapper.map(updatedPitch, PitchDto.class);
    }

    @Transactional()
    public void deletePitch(Long id){
        Optional<PitchEntity> existingPitch = pitchRepository.findByIdAndDeletedAtIsNull(id);
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(existingPitch.isEmpty()){
            throw new ApiException(HttpStatus.NOT_FOUND, "Pitch does not exist");
        }
        if(!userPrincipal.getUserEntity().getUserId().equals(existingPitch.get().getUserDetail().getUserId())){
            throw new ApiException(HttpStatus.FORBIDDEN, "You don't own this pitch");
        }
        PitchEntity pitchEntity = existingPitch.get();
        pitchEntity.setDeletedAt(LocalDateTime.now());
        pitchRepository.save(pitchEntity);
    }



    private SessionConfigurationDto createSessionConfiguration(PitchDto pitch) {
        SessionConfigurationDto sessionConfigurationDto = new SessionConfigurationDto();
        sessionConfigurationDto.setCreationMode(CreationMode.DAILY);
        sessionConfigurationDto.setNumberOfSessions(10);
        sessionConfigurationDto.setPitch(pitch);
        return sessionConfigurationDto;
    }

    public List<PitchDto> getAllPitches() {
        ModelMapper modelMapper = new ModelMapper();
        List<PitchEntity> pitchEntities = pitchRepository.findByDeletedAtIsNull();
        return pitchEntities.stream().map(pitch -> modelMapper.map(pitch, PitchDto.class)).toList();
    }

    public Page<PitchDto> getPitches(Pageable pageable) {
        ModelMapper modelMapper = new ModelMapper();
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userPrincipal.getUserEntity().getId();

        Page<PitchEntity> pitchEntities = pitchRepository.findByUserDetailIdAndDeletedAtIsNull(userId, pageable);

        return pitchEntities.map(pitch -> modelMapper.map(pitch, PitchDto.class));
    }

    public PitchDto getPitch(Long id) {
        ModelMapper modelMapper = new ModelMapper();
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = userPrincipal.getUserEntity().getUserId();
        Optional<PitchEntity> pitch = pitchRepository.findByIdAndDeletedAtIsNull(id);
        if (pitch.isEmpty()){
            throw new ApiException(HttpStatus.NOT_FOUND, "Pitch not found");
        }
        if(!pitch.get().getUserDetail().getUserId().equals(userId)){
            throw new ApiException(HttpStatus.FORBIDDEN, "You don't own this pitch");
        }
        PitchEntity pitchEntity = pitch.get();
        return modelMapper.map(pitchEntity, PitchDto.class);
    }
}
