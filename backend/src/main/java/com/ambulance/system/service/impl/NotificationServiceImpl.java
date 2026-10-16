package com.ambulance.system.service.impl;

import com.ambulance.system.dto.NotificationDTO;
import com.ambulance.system.entity.Booking;
import com.ambulance.system.entity.Notification;
import com.ambulance.system.entity.NotificationType;
import com.ambulance.system.entity.User;
import com.ambulance.system.exception.ResourceNotFoundException;
import com.ambulance.system.exception.UnauthorizedException;
import com.ambulance.system.mapper.EntityDtoMapper;
import com.ambulance.system.repository.NotificationRepository;
import com.ambulance.system.repository.UserRepository;
import com.ambulance.system.security.CustomUserDetails;
import com.ambulance.system.service.NotificationService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final EntityDtoMapper mapper;

    public NotificationServiceImpl(NotificationRepository notificationRepository, UserRepository userRepository, EntityDtoMapper mapper) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() instanceof String) {
            throw new UnauthorizedException("User is not authenticated");
        }
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new UnauthorizedException("User not found"));
    }

    @Override
    @Transactional
    public NotificationDTO sendNotification(User user, Booking booking, String title, String message, NotificationType type) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setBooking(booking);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type != null ? type : NotificationType.INFO);
        notification.setIsRead(false);

        Notification saved = notificationRepository.save(notification);
        return mapper.toNotificationDTO(saved);
    }

    @Override
    public List<NotificationDTO> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(mapper::toNotificationDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<NotificationDTO> getCurrentUserNotifications() {
        User user = getAuthenticatedUser();
        return getUserNotifications(user.getId());
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        User user = getAuthenticatedUser();
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));
        if (!n.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("Unauthorized access to notification");
        }
        n.setIsRead(true);
        notificationRepository.save(n);
    }

    @Override
    @Transactional
    public void markAllAsRead() {
        User user = getAuthenticatedUser();
        List<Notification> unread = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(user.getId());
        for (Notification n : unread) {
            n.setIsRead(true);
        }
        notificationRepository.saveAll(unread);
    }

    @Override
    public long getUnreadCount() {
        User user = getAuthenticatedUser();
        return notificationRepository.countByUserIdAndIsReadFalse(user.getId());
    }
}
