package com.mavi.kommo.exception;

public class KommoApiException extends RuntimeException {

    public KommoApiException(String message) {
        super(message);
    }

    public KommoApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
