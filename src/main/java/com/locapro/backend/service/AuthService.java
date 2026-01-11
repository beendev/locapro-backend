package com.locapro.backend.service;

import com.locapro.backend.dto.auth.LoginResult;
import com.locapro.backend.dto.auth.RegisterUserRequest;
import com.locapro.backend.dto.auth.UserResponse;
import com.locapro.backend.dto.common.ApiMessageResponse;

public interface AuthService {

    UserResponse register(RegisterUserRequest request);
    void verifyEmail(String rawToken);
    LoginResult login(String email, String rawPassword);
    LoginResult refresh(String refreshToken);
    void logout(String refreshToken);
    void logoutAll(Long userId);
    void logoutByRefreshToken(String refreshToken);
    void logoutAllDevices(Long userId);
    ApiMessageResponse resendEmailVerification(String email);
}
