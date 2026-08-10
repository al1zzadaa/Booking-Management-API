package com.example.bookingmanagementapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BookingManagementApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookingManagementApiApplication.class, args);
    }

}
