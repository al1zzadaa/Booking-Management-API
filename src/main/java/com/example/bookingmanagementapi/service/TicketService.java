package com.example.bookingmanagementapi.service;


import com.example.bookingmanagementapi.dto.filter.TicketFilter;
import com.example.bookingmanagementapi.dto.request.PaymentRequest;
import com.example.bookingmanagementapi.dto.request.TicketRequest;
import com.example.bookingmanagementapi.dto.request.UpdateTicketRequest;
import com.example.bookingmanagementapi.dto.response.flight.TicketResponse;

import java.util.List;

public interface TicketService {

    void book(TicketRequest ticketRequest);

    void payTicket(Long bookingId, PaymentRequest request);

    void cancel(Long ticketId);

    List<TicketResponse> findAll(TicketFilter ticketFilter);

    TicketResponse findById(Long id);

    void updateTicket(Long id, UpdateTicketRequest updateTicketRequest);

    void deleteTicketById(Long id);


}
