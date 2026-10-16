package com.ambulance.system.dto;

import java.util.List;

public class AdminDashboardDTO {
    private long totalUsers;
    private long totalDrivers;
    private long totalAmbulances;
    private long availableAmbulances;
    private long busyAmbulances;
    private long activeBookings;
    private long completedBookings;
    private long cancelledBookings;
    private long emergencyBookings;
    private long onlineDrivers;
    private List<BookingDTO> recentBookings;

    public AdminDashboardDTO() {
    }

    public AdminDashboardDTO(long totalUsers, long totalDrivers, long totalAmbulances, long availableAmbulances,
                             long busyAmbulances, long activeBookings, long completedBookings, long cancelledBookings,
                             long emergencyBookings, long onlineDrivers, List<BookingDTO> recentBookings) {
        this.totalUsers = totalUsers;
        this.totalDrivers = totalDrivers;
        this.totalAmbulances = totalAmbulances;
        this.availableAmbulances = availableAmbulances;
        this.busyAmbulances = busyAmbulances;
        this.activeBookings = activeBookings;
        this.completedBookings = completedBookings;
        this.cancelledBookings = cancelledBookings;
        this.emergencyBookings = emergencyBookings;
        this.onlineDrivers = onlineDrivers;
        this.recentBookings = recentBookings;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalDrivers() {
        return totalDrivers;
    }

    public void setTotalDrivers(long totalDrivers) {
        this.totalDrivers = totalDrivers;
    }

    public long getTotalAmbulances() {
        return totalAmbulances;
    }

    public void setTotalAmbulances(long totalAmbulances) {
        this.totalAmbulances = totalAmbulances;
    }

    public long getAvailableAmbulances() {
        return availableAmbulances;
    }

    public void setAvailableAmbulances(long availableAmbulances) {
        this.availableAmbulances = availableAmbulances;
    }

    public long getBusyAmbulances() {
        return busyAmbulances;
    }

    public void setBusyAmbulances(long busyAmbulances) {
        this.busyAmbulances = busyAmbulances;
    }

    public long getActiveBookings() {
        return activeBookings;
    }

    public void setActiveBookings(long activeBookings) {
        this.activeBookings = activeBookings;
    }

    public long getCompletedBookings() {
        return completedBookings;
    }

    public void setCompletedBookings(long completedBookings) {
        this.completedBookings = completedBookings;
    }

    public long getCancelledBookings() {
        return cancelledBookings;
    }

    public void setCancelledBookings(long cancelledBookings) {
        this.cancelledBookings = cancelledBookings;
    }

    public long getEmergencyBookings() {
        return emergencyBookings;
    }

    public void setEmergencyBookings(long emergencyBookings) {
        this.emergencyBookings = emergencyBookings;
    }

    public long getOnlineDrivers() {
        return onlineDrivers;
    }

    public void setOnlineDrivers(long onlineDrivers) {
        this.onlineDrivers = onlineDrivers;
    }

    public List<BookingDTO> getRecentBookings() {
        return recentBookings;
    }

    public void setRecentBookings(List<BookingDTO> recentBookings) {
        this.recentBookings = recentBookings;
    }
}
