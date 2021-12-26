package com.github.eros.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;

/**
 * @author fankongqiumu
 * @description
 * @date 2021/12/21 16:36
 */
@SpringBootApplication
@ConditionalOnNotWebApplication
public class ServerBootStrap{
    public static void main(String[] args) {
        SpringApplication.run(ServerBootStrap.class, args);
    }
}
