package com.example.bookingmanagementapi.controller;

import com.example.bookingmanagementapi.dto.response.NotificationResponse;
import com.example.bookingmanagementapi.security.CustomUserDetails;
import com.example.bookingmanagementapi.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id,
                       @AuthenticationPrincipal CustomUserDetails user
    ) {
        notificationService.delete(id, user.getId());
    }

    @GetMapping
    public Page<NotificationResponse> getAll(@AuthenticationPrincipal CustomUserDetails user,
                                             Pageable pageable
    ) {
        return notificationService.getAll(user.getId(), pageable);
    }

    @GetMapping("/unread")
    public Page<NotificationResponse> getUnread(@AuthenticationPrincipal CustomUserDetails user,
                                                Pageable pageable
    ) {
        return notificationService.getUnreadNotifications(
                user.getId(),
                pageable
        );
    }

    @GetMapping("/{id}")
    public NotificationResponse getById(@PathVariable Long id,
                                        @AuthenticationPrincipal CustomUserDetails user
    ) {
        return notificationService.getById(id, user.getId());
    }

    @PatchMapping("/{id}/read")
    public void markAsRead(@PathVariable Long id,
                           @AuthenticationPrincipal CustomUserDetails user
    ) {
        notificationService.markAsRead(id, user.getId());
    }
}
