package com.payment.erc20;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@ComponentScan(value = "com.payment.core")
@ComponentScan(value = "com.payment.core.exception")
@ComponentScan(value = "com.payment.erc20")
@ComponentScan(value = "com.payment.ethereum")
@EntityScan(basePackages = {"com.payment.core.entity"})
@EnableJpaRepositories(basePackages = {"com.payment.core.repository"})
public class Erc20Application {

    public static void main(String[] args) {
        SpringApplication.run(Erc20Application.class, args);
    }
}
