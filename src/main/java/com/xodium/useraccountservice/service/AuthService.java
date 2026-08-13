package com.xodium.useraccountservice.service;

import com.xodium.useraccountservice.dto.ApiResponse;
import com.xodium.useraccountservice.dto.AuthResponse;
import com.xodium.useraccountservice.dto.LoginRequest;
import com.xodium.useraccountservice.dto.RegistrationRequest;

public interface AuthService {
    ApiResponse<AuthResponse> registerUser(RegistrationRequest registrationRequest);

    ApiResponse<AuthResponse> loginUser(LoginRequest loginRequest);
}
