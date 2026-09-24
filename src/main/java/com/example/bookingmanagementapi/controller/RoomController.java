package com.example.bookingmanagementapi.controller;

import com.example.bookingmanagementapi.dto.filter.RoomFilter;
import com.example.bookingmanagementapi.dto.request.RoomRequest;
import com.example.bookingmanagementapi.dto.response.hotel.RoomResponse;
import com.example.bookingmanagementapi.service.RoomService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @DeleteMapping("/{id}")
    public void deleteRoom(@PathVariable @Positive Long id) {
        roomService.deleteRoom(id);
    }

    @PostMapping
    public void createRoom(@Valid @RequestBody RoomRequest roomRequest) {
        roomService.createRoom(roomRequest);
    }

    @GetMapping("/{id}")
    public RoomResponse getRoomById(@PathVariable @Positive Long id) {
        return roomService.findById(id);
    }

    @GetMapping
    public Page<@NonNull RoomResponse> getRooms(RoomFilter roomFilter, Pageable pageable) {
        return roomService.findAll(roomFilter, pageable);
    }

    @GetMapping("/number")
    public RoomResponse findByRoomNo(@RequestParam @Positive Integer roomNo) {
        return roomService.findByRoomNo(roomNo);
    }


}
