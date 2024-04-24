package com.bank.light.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile(TestProfile.PROFILE_NAME)
public class TestProfile {
    public static final String PROFILE_NAME = "test";
}
