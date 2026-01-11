// com.locapro.backend.exception.UnauthorizedException.java
package com.locapro.backend.exception;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
