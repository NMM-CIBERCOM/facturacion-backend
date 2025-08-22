package com.cibercom.pac;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PacSimulatorApplication {
    public static void main(String[] args) {
        SpringApplication.run(PacSimulatorApplication.class, args);
    }
}


