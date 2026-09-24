package com.example.bookingmanagementapi.controller;

import com.example.bookingmanagementapi.dto.filter.PromoCodeFilter;
import com.example.bookingmanagementapi.dto.request.PromoCodeRequest;
import com.example.bookingmanagementapi.dto.response.PromoCodeResponse;
import com.example.bookingmanagementapi.service.PromoCodeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/promo-codes")
@RequiredArgsConstructor
public class PromoCodeController {

    private final PromoCodeService promoCodeService;

    @PostMapping
    public void create(@Valid @RequestBody PromoCodeRequest request) {
        promoCodeService.create(request);
    }

    @GetMapping
    public Page<@NonNull PromoCodeResponse> getAll(PromoCodeFilter promoCodeFilter, Pageable pageable) {
        return promoCodeService.getAll(promoCodeFilter, pageable);
    }

    @GetMapping("/{id}")
    public PromoCodeResponse getById(@PathVariable @Positive Long id) {
        return promoCodeService.getById(id);
    }

    @PutMapping("/{id}")
    public void update(
            @PathVariable @Positive Long id,
            @RequestBody PromoCodeRequest request
    ) {
        promoCodeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable @Positive Long id) {
        promoCodeService.deleteById(id);
    }

    @PatchMapping("/{id}/activate")
    public void activate(@PathVariable @Positive Long id) {
        promoCodeService.activate(id);
    }

    @PatchMapping("/{id}/deactivate")
    public void deactivate(@PathVariable @Positive Long id) {
        promoCodeService.deactivate(id);
    }

}
