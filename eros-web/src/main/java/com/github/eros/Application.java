package com.github.eros;

import com.github.eros.starter.annotation.EnableEros;
import com.github.nameserver.starter.annotation.EnableNameServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @author fankongqiumu
 * @Description
 * @date 2021/12/17 11:37
 */
@SpringBootApplication
@ServletComponentScan
@EnableAsync
@EnableNameServer
@EnableEros
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
