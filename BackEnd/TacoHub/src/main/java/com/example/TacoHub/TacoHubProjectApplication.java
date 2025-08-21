package com.example.TacoHub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(
  info = @Info(title = "TacoHub API", version = "v1", description = "TacoHub 서비스 API 문서")
)
@SpringBootApplication
@EnableJpaAuditing
public class TacoHubProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(TacoHubProjectApplication.class, args);
	}

}
