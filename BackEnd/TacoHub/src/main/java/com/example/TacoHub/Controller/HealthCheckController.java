package com.example.TacoHub.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.TacoHub.Dto.NotionCopyDTO.Response.ApiResponse;

@RestController
@RequestMapping("/api/actuator")
public class HealthCheckController {

    @GetMapping("/health")
    public ApiResponse<String> checkHealth() {
        return ApiResponse.success("Healthy", null);
    }
}
