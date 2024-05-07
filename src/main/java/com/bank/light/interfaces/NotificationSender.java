package com.bank.light.interfaces;

import com.bank.light.models.NotificationMsg;

public interface NotificationSender {
    void enqueueNotification(final NotificationMsg notificationMsg);
}
