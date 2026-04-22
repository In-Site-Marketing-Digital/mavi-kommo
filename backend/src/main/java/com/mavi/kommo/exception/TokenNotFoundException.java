package com.mavi.kommo.exception;

public class TokenNotFoundException extends RuntimeException {

    public TokenNotFoundException() {
        super("Kommo account not connected. Please authenticate first.");
    }
}
