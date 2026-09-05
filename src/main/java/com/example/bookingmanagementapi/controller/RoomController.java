package com.example.bookingmanagementapi.controller;

import com.example.bookingmanagementapi.dto.filter.RoomFilter;
import com.example.bookingmanagementapi.dto.request.RoomRequest;
import com.example.bookingmanagementapi.dto.response.hotel.RoomResponse;
import com.example.bookingmanagementapi.service.RoomService;
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
    public void deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
    }

    @PostMapping
    public void createRoom(@RequestBody RoomRequest roomRequest) {
        roomService.createRoom(roomRequest);
    }

    @GetMapping("/{id}")
    public RoomResponse getRoomById(@PathVariable Long id) {
        return roomService.findById(id);
    }

    @GetMapping
    public Page<RoomResponse> getRooms(RoomFilter roomFilter, Pageable pageable) {
        return roomService.findAll(roomFilter, pageable);
    }

    @GetMapping("/number")
    public RoomResponse findByRoomNo(@RequestParam Integer roomNo) {
        return roomService.findByRoomNo(roomNo);
    }


}
