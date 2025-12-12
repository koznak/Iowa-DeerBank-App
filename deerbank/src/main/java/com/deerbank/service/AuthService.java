package com.deerbank.service;

import com.deerbank.dto.*;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    LoginResponse register(RegisterRequest request);

    UpdatePasswordResponse updatePassword(UpdatePasswordRequest request);
}
