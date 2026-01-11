// com.locapro.backend.dto.auth.ForgotPasswordRequest
package com.locapro.backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(
        @NotBlank @Email String email
) {}
