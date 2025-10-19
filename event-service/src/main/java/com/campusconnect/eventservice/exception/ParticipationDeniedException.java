package com.campusconnect.eventservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class ParticipationDeniedException extends RuntimeException {
    public ParticipationDeniedException(String message) {
        super(message);
    }
}