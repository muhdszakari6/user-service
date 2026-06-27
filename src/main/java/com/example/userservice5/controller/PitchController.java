package com.example.userservice5.controller;

import com.example.userservice5.dto.PitchDto;
import com.example.userservice5.dto.UserDto;
import com.example.userservice5.model.request.CreatePitchRequest;
import com.example.userservice5.model.request.UpdatePitchRequest;
import com.example.userservice5.model.response.*;
import com.example.userservice5.service.PitchService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("pitch")
@RequiredArgsConstructor
public class PitchController {
    private final PitchService pitchService;

    @PreAuthorize("hasRole('PARTNER')")
    @PostMapping(path = "", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<CreatePitchResponse> createPitch(@RequestBody @Valid CreatePitchRequest requestBody){
        ModelMapper mapper = new ModelMapper();
        PitchDto pitchDto = mapper.map(requestBody, PitchDto.class);
        PitchDto returnValue = this.pitchService.createPitch(pitchDto);
        return ResponseEntity.status(200).body(mapper.map(returnValue, CreatePitchResponse.class));
    }

    @PreAuthorize("hasRole('PARTNER')")
    @PutMapping(path = "/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<UpdatePitchResponse> updatePitch(@RequestBody @Valid UpdatePitchRequest requestBody, @PathVariable Long id){
        ModelMapper mapper = new ModelMapper();
        PitchDto pitchDto = mapper.map(requestBody, PitchDto.class);
        PitchDto returnValue = this.pitchService.updatePitch(pitchDto, id);
        return ResponseEntity.status(200).body(mapper.map(returnValue, UpdatePitchResponse.class));
    }

    @PreAuthorize("hasRole('PARTNER')")
    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deletePitch(@PathVariable Long id){
        this.pitchService.deletePitch(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(path = "/public", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<GetPitchesResponse>> getPublicPitches(){
        ModelMapper mapper = new ModelMapper();
        List<GetPitchesResponse> response = this.pitchService.getAllPitches()
                .stream()
                .map(pitchDto -> mapper.map(pitchDto, GetPitchesResponse.class))
                .toList();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PARTNER')")
    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Page<GetPitchesResponse>> getPitches(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size
    ){
        Pageable pageable = PageRequest.of(page, size);
        ModelMapper mapper = new ModelMapper();
        Page<PitchDto> pitches = this.pitchService.getPitches(pageable);
        Page<GetPitchesResponse> response = pitches.map(pitchDto -> mapper.map(pitchDto, GetPitchesResponse.class));
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PARTNER')")
    @GetMapping(path = "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<GetPitchResponse> getPitch(@PathVariable Long id){
        ModelMapper mapper = new ModelMapper();
        PitchDto pitch = this.pitchService.getPitch(id);
        GetPitchResponse response = mapper.map(pitch, GetPitchResponse.class);
        return ResponseEntity.ok(response);

    }

}
