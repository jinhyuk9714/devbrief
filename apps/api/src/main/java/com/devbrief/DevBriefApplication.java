package com.devbrief;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DevBriefApplication {
    public static void main(String[] args) {
        SpringApplication.run(DevBriefApplication.class, args);
    }
}

