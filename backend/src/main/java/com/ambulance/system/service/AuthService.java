package com.ambulance.system.service;

import com.ambulance.system.dto.LoginRequest;
import com.ambulance.system.dto.LoginResponse;
import com.ambulance.system.dto.RegisterRequest;
import com.ambulance.system.dto.UserDTO;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    UserDTO register(RegisterRequest request);
    UserDTO getCurrentUser();
}
