package com.project;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class HufsDevApplication {

    public static void main(String[] args) {
        SpringApplication.run(HufsDevApplication.class, args);
    }

    @PostConstruct
    void setTimezone() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }
}
