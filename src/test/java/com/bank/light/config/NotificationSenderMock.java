package com.bank.light.config;

import com.bank.light.interfaces.NotificationSender;
import com.bank.light.models.NotificationMsg;
import org.springframework.stereotype.Service;

@Service
public class NotificationSenderMock implements NotificationSender {

    public void enqueueNotification(final NotificationMsg notificationMsg) {

    }
}
