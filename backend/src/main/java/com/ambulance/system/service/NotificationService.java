package com.ambulance.system.service;

import com.ambulance.system.dto.NotificationDTO;
import com.ambulance.system.entity.Booking;
import com.ambulance.system.entity.NotificationType;
import com.ambulance.system.entity.User;

import java.util.List;

public interface NotificationService {
    NotificationDTO sendNotification(User user, Booking booking, String title, String message, NotificationType type);
    List<NotificationDTO> getUserNotifications(Long userId);
    List<NotificationDTO> getCurrentUserNotifications();
    void markAsRead(Long notificationId);
    void markAllAsRead();
    long getUnreadCount();
}
