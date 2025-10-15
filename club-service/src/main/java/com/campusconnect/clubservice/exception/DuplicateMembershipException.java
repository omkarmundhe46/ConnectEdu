package com.campusconnect.clubservice.exception;

public class DuplicateMembershipException extends RuntimeException {
    public DuplicateMembershipException(String message) {
        super(message);
    }
}