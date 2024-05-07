package com.bank.light.services.rabbitmq;

import com.bank.light.interfaces.NotificationSender;
import com.bank.light.models.NotificationMsg;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationSenderImpl implements NotificationSender {

    private final Queue queue;

    private final RabbitTemplate rabbitTemplate;

    public NotificationSenderImpl(final Queue queue, final RabbitTemplate rabbitTemplate) {
        this.queue = queue;
        this.rabbitTemplate = rabbitTemplate;
    }

    public void enqueueNotification(final NotificationMsg notificationMsg) {
        rabbitTemplate.convertAndSend(queue.getName(), notificationMsg);
    }
}
