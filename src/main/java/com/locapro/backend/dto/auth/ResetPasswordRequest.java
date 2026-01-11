// com.locapro.backend.dto.auth.ResetPasswordRequest
package com.locapro.backend.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(
        @NotBlank String token,
        @NotBlank String newPassword
) {}
