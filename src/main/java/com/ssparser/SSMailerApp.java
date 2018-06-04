package com.ssparser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableAutoConfiguration
@ComponentScan("com.ssparser")
@EnableScheduling
public class SSMailerApp {

    private static final Logger log = LoggerFactory.getLogger(SSMailerApp.class);

    public static void main(String[] args) {
        SpringApplication.run(SSMailerApp.class);
    }
}
