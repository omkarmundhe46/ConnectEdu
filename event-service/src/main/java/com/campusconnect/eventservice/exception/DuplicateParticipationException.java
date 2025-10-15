package com.campusconnect.eventservice.exception;

public class DuplicateParticipationException extends RuntimeException {
    public DuplicateParticipationException(String message) {
        super(message);
    }
}