package com.deyan.mealplanner.exceptions;

public class ExternalApiQuotaException extends RuntimeException {

    public ExternalApiQuotaException() {
        super("Daily recipe quota reached, please try again tomorrow.");
    }

    public ExternalApiQuotaException(String message) {
        super(message);
    }
}
