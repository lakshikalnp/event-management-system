package com.example.eventmanagement.util;

import java.time.format.DateTimeFormatter;

public class AppConstants {

    private AppConstants() {}

    // General constants
    public static final String SUCCESSFULLY_CREATED_AN_EVENT = "An event created successfully";
    public static final String SUCCESSFULLY_FETCHED ="Successfully fetched";
    public static final String NOT_FOUND_EVENT = "Event not found with id: ";
    public static final String EXCEPTION_OCCURED = "Exception occurred: ";

    public static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
}
