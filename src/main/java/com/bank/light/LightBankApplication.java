package com.bank.light;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class LightBankApplication {
    public static void main(String[] args) {
        SpringApplication.run(LightBankApplication.class, args);
    }
}
