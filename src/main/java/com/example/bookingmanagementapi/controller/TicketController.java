package com.example.bookingmanagementapi.controller;

import com.example.bookingmanagementapi.dto.filter.TicketFilter;
import com.example.bookingmanagementapi.dto.request.PaymentRequest;
import com.example.bookingmanagementapi.dto.request.TicketRequest;
import com.example.bookingmanagementapi.dto.request.UpdateTicketRequest;
import com.example.bookingmanagementapi.dto.response.FlightBookingResponse;
import com.example.bookingmanagementapi.dto.response.flight.TicketResponse;
import com.example.bookingmanagementapi.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    public void bookTicket(@RequestBody TicketRequest ticketRequest,
                           @AuthenticationPrincipal UserDetails userDetails) {
        ticketService.book(ticketRequest, userDetails.getUsername());
    }

    @GetMapping("/{id}")
    public TicketResponse getTicketById(@PathVariable Long id) {
        return ticketService.findById(id);
    }

    @GetMapping
    public List<TicketResponse> getTickets(TicketFilter ticketFilter) {
        return ticketService.findAll(ticketFilter);
    }

    @PutMapping("/{id}")
    public void updateTicket(@PathVariable Long id, @RequestBody UpdateTicketRequest updateTicketRequest) {
        ticketService.updateTicket(id, updateTicketRequest);
    }

    @DeleteMapping("/{id}")
    public void deleteTicketById(@PathVariable Long id) {
        ticketService.deleteTicketById(id);
    }

    @PostMapping("/flight-bookings/{flightBookingId}/pay")
    public void payFlightBooking(@AuthenticationPrincipal UserDetails userDetails,
                                 @PathVariable Long flightBookingId,
                                 @RequestBody PaymentRequest request) {
        ticketService.payTicket(
                userDetails.getUsername(),
                flightBookingId,
                request);
    }

    @PostMapping("/cancel/{flightBookingId}")
    public void cancel(@AuthenticationPrincipal UserDetails userDetails,
                       @PathVariable Long flightBookingId) {
        ticketService.cancel(userDetails.getUsername(), flightBookingId);
    }

    @GetMapping("/my-history")
    public Page<FlightBookingResponse> getMyBookings(@AuthenticationPrincipal UserDetails userDetails, Pageable pageable) {
        // Pass the username/email to the service to fetch only their bookings
        return ticketService.getUserTickets(userDetails.getUsername(), pageable);
    }

}
