// com.locapro.backend.exception.ForbiddenException.java
package com.locapro.backend.exception;

public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
