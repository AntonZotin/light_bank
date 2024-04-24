package com.bank.light.interfaces;

import com.bank.light.domain.Notification;
import java.util.List;

public interface NotificationService {
    void notifyManagers(String username, String message);

    List<Notification> notificationList(Integer page, String username);

    int countByUsername(String username);

    Long pageCount(String username);

    int countUnread(String username);

    void readNotification(Long id);
}
