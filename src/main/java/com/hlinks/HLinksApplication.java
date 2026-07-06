package com.hlinks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
@EnableAsync
public class HLinksApplication {

    public static void main(String[] args) {
        SpringApplication.run(HLinksApplication.class, args);
    }

}
