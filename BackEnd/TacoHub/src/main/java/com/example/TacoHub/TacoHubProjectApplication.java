package com.example.TacoHub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class TacoHubProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(TacoHubProjectApplication.class, args);
	}

}
