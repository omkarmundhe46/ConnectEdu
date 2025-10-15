package com.campusconnect.clubservice.exception;

public class ClubNotFoundException extends RuntimeException {
    public ClubNotFoundException(String message) {
        super(message);
    }
}