package com.campusconnect.discussionservice.exception;

public class NotMemberException extends RuntimeException {
    public NotMemberException(String message) {
        super(message);
    }
}