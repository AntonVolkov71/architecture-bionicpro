package ru.volkov.report;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        System.out.println("[SCENARIO-SERVICE] started");

        SpringApplication.run(Application.class, args);
    }
}
