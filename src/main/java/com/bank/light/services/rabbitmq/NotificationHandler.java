package com.bank.light.services.rabbitmq;

import com.bank.light.interfaces.AccountService;
import com.bank.light.interfaces.NotificationService;
import com.bank.light.models.NotificationMsg;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationHandler {

    private final NotificationService notificationService;

    private final AccountService accountService;

    public NotificationHandler(final NotificationService notificationService, final AccountService accountService) {
        this.notificationService = notificationService;
        this.accountService = accountService;
    }

    @RabbitListener(queues = "${notificationQueueName}")
    public void receiveNotification(final NotificationMsg notificationMsg) {
        notificationService.notifyManagers(accountService.getUsername(
                        notificationMsg.paymentAccount()),
                notificationMsg.message());
    }
}
