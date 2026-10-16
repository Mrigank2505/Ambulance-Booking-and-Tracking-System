package com.ambulance.system.dto;

import jakarta.validation.constraints.NotBlank;

public class EmergencyContactDTO {
    private Long id;
    private Long userId;

    @NotBlank(message = "Contact name is required")
    private String name;

    @NotBlank(message = "Contact phone is required")
    private String phone;

    private String relationship;

    public EmergencyContactDTO() {
    }

    public EmergencyContactDTO(Long id, Long userId, String name, String phone, String relationship) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.phone = phone;
        this.relationship = relationship;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }
}
