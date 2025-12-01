package com.deerbank.service;

import com.deerbank.dto.LoginRequest;
import com.deerbank.dto.LoginResponse;
import com.deerbank.dto.RegisterRequest;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    LoginResponse register(RegisterRequest request);

}
