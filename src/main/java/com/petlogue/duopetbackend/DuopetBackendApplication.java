package com.petlogue.duopetbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class DuopetBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(DuopetBackendApplication.class, args);
    }

}
