package com.github.eros.demo;

import com.github.eros.starter.annotation.EnableEros;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableEros
public class DemoApp {
    public static void main(String[] args) {
        SpringApplication.run(DemoApp.class, args);
        for (;;){}
    }
}
