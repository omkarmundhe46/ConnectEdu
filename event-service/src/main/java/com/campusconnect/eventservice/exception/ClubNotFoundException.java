package com.campusconnect.eventservice.exception;

public class ClubNotFoundException extends RuntimeException {
    public ClubNotFoundException(String message) {
        super(message);
    }
}