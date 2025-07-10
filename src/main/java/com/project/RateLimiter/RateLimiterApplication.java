package com.project.RateLimiter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class RateLimiterApplication {

	public static void main(String[] args) {
		log.info("Starting RateLimiterApplication");
		SpringApplication.run(RateLimiterApplication.class, args);
	}

}
