package com.example.bookingmanagementapi.controller;

import com.example.bookingmanagementapi.dto.request.NotificationRequest;
import com.example.bookingmanagementapi.dto.request.UpdateNotificationRequest;
import com.example.bookingmanagementapi.dto.response.NotificationResponse;
import com.example.bookingmanagementapi.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

//    @PostMapping
//    void create(NotificationRequest notificationRequest){
//        notificationService.create(notificationRequest);
//    }

//    @GetMapping("/{id}")
//    NotificationResponse getById(@PathVariable Long id){
//        return notificationService.getById(id);
//    }

//    @GetMapping("/user/{userId}")
//    Page<NotificationResponse> getAll(@PathVariable Long userId,  Pageable pageable){
//        return notificationService.getAll(userId,  pageable);
//    }

//    @PutMapping("/{id}")
//    void update(@PathVariable Long id, @RequestBody UpdateNotificationRequest updateNotificationRequest){
//        notificationService.update(id, updateNotificationRequest);
//    }/

//    @DeleteMapping("/{id}")
//    void delete(@PathVariable Long id){
//        notificationService.delete(id);
//    }

    @PostMapping("/send/{userId}")
    void send(@PathVariable Long userId, NotificationRequest notificationRequest){
        notificationService.send(userId, notificationRequest);
    }

    @PostMapping("/markRead/{notificationId}")
    void markAsRead(@PathVariable Long notificationId){
        notificationService.markAsRead(notificationId);
    }

    @GetMapping("/user/unread/{userId}")
    Page<NotificationResponse> getUnreadNotifications(@PathVariable Long userId, Pageable pageable){
        return notificationService.getUnreadNotifications(userId, pageable);
    }
}
