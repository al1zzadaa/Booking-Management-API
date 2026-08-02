package com.example.bookingmanagementapi.controller;

import com.example.bookingmanagementapi.dto.filter.TicketFilter;
import com.example.bookingmanagementapi.dto.request.PaymentRequest;
import com.example.bookingmanagementapi.dto.request.TicketRequest;
import com.example.bookingmanagementapi.dto.request.UpdateTicketRequest;
import com.example.bookingmanagementapi.dto.response.flight.TicketResponse;
import com.example.bookingmanagementapi.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    public void bookTicket(@RequestBody TicketRequest ticketRequest){
        ticketService.book(ticketRequest);
    }

    @GetMapping("/{id}")
    public TicketResponse getTicketById(@PathVariable Long id){
        return ticketService.findById(id);
    }

    @GetMapping
    public List<TicketResponse> getTickets(TicketFilter ticketFilter){
        return ticketService.findAll(ticketFilter);
    }

    @PutMapping("/{id}")
    public void updateTicket(@PathVariable Long id, @RequestBody UpdateTicketRequest updateTicketRequest){
        ticketService.updateTicket(id, updateTicketRequest);
    }

    @DeleteMapping("/{id}")
    public void deleteTicketById(@PathVariable Long id){
        ticketService.deleteTicketById(id);
    }

    @PostMapping("/pay/{ticketId}")
    public void payTicket(@PathVariable Long ticketId, @RequestBody PaymentRequest request) {
        ticketService.payTicket(ticketId, request);
    }

    @PostMapping("/cancel/{ticketId}")
    public void cancel(@PathVariable Long ticketId){
        ticketService.cancel(ticketId);
    }

}
