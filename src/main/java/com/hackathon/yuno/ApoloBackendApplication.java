package com.hackathon.yuno;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ApoloBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApoloBackendApplication.class, args);
	}

}
