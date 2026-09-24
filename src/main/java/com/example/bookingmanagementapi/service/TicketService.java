package com.example.bookingmanagementapi.service;


import com.example.bookingmanagementapi.dto.filter.TicketFilter;
import com.example.bookingmanagementapi.dto.request.PaymentRequest;
import com.example.bookingmanagementapi.dto.request.TicketRequest;
import com.example.bookingmanagementapi.dto.response.FlightBookingResponse;
import com.example.bookingmanagementapi.dto.response.flight.TicketResponse;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TicketService {

    void bookTicket(TicketRequest ticketRequest, String username);

    void payTicket(String username,
                    Long flightBookingId,
                    PaymentRequest request);

    void refundTicket(String username, Long flightBookingId);

    List<TicketResponse> findAll(TicketFilter ticketFilter);

    TicketResponse findById(Long id);

    void deleteTicketById(Long id);

    Page<@NonNull FlightBookingResponse> getUserTickets(String username, Pageable pageable);
}
