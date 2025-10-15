package com.campusconnect.clubservice.exception;

public class ClubNameAlreadyExistsException extends RuntimeException {
    public ClubNameAlreadyExistsException(String message) {
        super(message);
    }
}