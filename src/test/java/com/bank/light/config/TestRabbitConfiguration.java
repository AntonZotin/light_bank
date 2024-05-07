package com.bank.light.config;

import com.github.fridujo.rabbitmq.mock.compatibility.MockConnectionFactoryFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestRabbitConfiguration {
    @Bean
    ConnectionFactory connectionFactory() {
        return new CachingConnectionFactory(MockConnectionFactoryFactory.build());
    }
}
