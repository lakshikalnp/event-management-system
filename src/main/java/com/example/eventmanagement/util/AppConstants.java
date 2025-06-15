package com.example.eventmanagement.util;

import java.time.format.DateTimeFormatter;

public class AppConstants {

    public static final String X_RETRY_COUNT = "x-retry-count";

    private AppConstants() {}

    // General constants
    public static final String SUCCESSFULLY_CREATED_AN_EVENT = "Event successfully added with id: ";
    public static final String SUCCESSFULLY_FETCHED ="Successfully fetched";
    public static final String NOT_FOUND_EVENT = "Event not found with id: ";
    public static final String EXCEPTION_OCCURRED = "Exception occurred: ";
    public static final String  SUCCESSFULLY_UPDATED_AN_EVENT = "Event successfully updated with id: ";
    public static final String SUCCESSFULLY_DELETED_AN_EVENT = "Event successfully deleted with id: ";

    //Error codes
    public static final String UN_AUTHORIZED_ERROR_CODE = "UNAUTHORIZED";
    public static final String INTERNAL_ERROR_CODE = "INTERNAL_ERROR";
    public static final String RESOURCE_NOT_FOUND_ERROR_CODE = "RESOURCE_NOT_FOUND";
    public static final String ACCESS_DENIED_ERROR_CODE = "ACCESS_DENIED";
    public static final String VALIDATION_FAILURE_CODE = "VALIDATION_FAILURE";

    public static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
}
