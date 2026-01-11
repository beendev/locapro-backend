// com.locapro.backend.dto.common.ApiErrorResponse.java
package com.locapro.backend.dto.common;

import java.time.OffsetDateTime;
import java.util.Map;

public class ApiErrorResponse {

    private OffsetDateTime timestamp;
    private int status;
    private String error;
    private String code;          // ex: "VALIDATION_ERROR", "BAD_REQUEST"
    private String message;       // message lisible
    private String path;          // /auth/register
    private Map<String, String> validationErrors; // champ -> erreur (optionnel)

    public ApiErrorResponse() {
    }

    public ApiErrorResponse(OffsetDateTime timestamp, int status, String error,
                            String code, String message, String path,
                            Map<String, String> validationErrors) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.code = code;
        this.message = message;
        this.path = path;
        this.validationErrors = validationErrors;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, String> getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(Map<String, String> validationErrors) {
        this.validationErrors = validationErrors;
    }
}
