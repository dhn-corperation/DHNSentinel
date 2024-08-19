package com.dhn.sentinel.dhnsentinel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DhnSentinelApplication {

    public static void main(String[] args) {
        SpringApplication.run(DhnSentinelApplication.class, args);
    }

}
