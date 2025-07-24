package com.example.TacoHub.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/acutator")
public class HealthCheckController {

    @GetMapping("/health")
    public String checkHealth() {
        return "Healthy";
    }
}
