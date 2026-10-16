package com.ambulance.system.service;

import com.ambulance.system.dto.AdminDashboardDTO;
import com.ambulance.system.dto.UserDTO;
import com.ambulance.system.entity.Role;
import com.ambulance.system.entity.UserStatus;

import java.util.List;

public interface AdminService {
    AdminDashboardDTO getDashboardStats();
    List<UserDTO> getAllUsers();
    UserDTO updateUserStatus(Long userId, UserStatus status);
    UserDTO updateUserRole(Long userId, Role role);
}
