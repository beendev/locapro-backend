// com.locapro.backend.exception.NotFoundException.java
package com.locapro.backend.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
