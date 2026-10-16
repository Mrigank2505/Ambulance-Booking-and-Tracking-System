package com.ambulance.system.service;

import com.ambulance.system.dto.EmergencyContactDTO;
import com.ambulance.system.dto.PasswordChangeRequest;
import com.ambulance.system.dto.UserDTO;
import com.ambulance.system.dto.UserProfileUpdateRequest;

import java.util.List;

public interface UserService {
    UserDTO getCurrentUserProfile();
    UserDTO updateProfile(UserProfileUpdateRequest request);
    void changePassword(PasswordChangeRequest request);
    List<EmergencyContactDTO> getEmergencyContacts();
    EmergencyContactDTO addEmergencyContact(EmergencyContactDTO request);
    void deleteEmergencyContact(Long contactId);
}
