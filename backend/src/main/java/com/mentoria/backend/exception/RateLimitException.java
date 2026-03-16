package com.mentoria.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class RateLimitException extends RuntimeException {

    private final int remaining;

    public RateLimitException(String message, int remaining) {
        super(message);
        this.remaining = remaining;
    }

    public int getRemaining() {
        return remaining;
    }
}