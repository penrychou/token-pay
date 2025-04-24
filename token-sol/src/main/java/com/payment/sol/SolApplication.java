package com.payment.sol;

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
@ComponentScan(value = "com.payment.sol")
@EntityScan(basePackages = {"com.payment.core.entity"})
@EnableJpaRepositories(basePackages = {"com.payment.core.repository"})
public class SolApplication {

    public static void main(String[] args) {
        SpringApplication.run(SolApplication.class, args);
    }
}
