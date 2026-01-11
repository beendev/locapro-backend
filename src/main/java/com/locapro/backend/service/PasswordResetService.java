// com.locapro.backend.service.PasswordResetService
package com.locapro.backend.service;

import com.locapro.backend.dto.auth.ForgotPasswordRequest;
import com.locapro.backend.dto.auth.ResetPasswordRequest;
import com.locapro.backend.dto.common.ApiMessageResponse;

public interface PasswordResetService {
    void requestReset(ForgotPasswordRequest req);
    ApiMessageResponse resetPassword(ResetPasswordRequest req);
}
