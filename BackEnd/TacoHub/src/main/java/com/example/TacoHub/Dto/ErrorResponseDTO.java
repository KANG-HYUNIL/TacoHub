package com.example.TacoHub.Dto;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class ErrorResponseDTO {
    
        public String error;
        public String message;
        public LocalDateTime timestamp;
        public Map<String, String> details;

        public ErrorResponseDTO(String error, String message) {
            this.error = error;
            this.message = message;
            this.timestamp = LocalDateTime.now();
            this.details = new HashMap<>();
        }

        public ErrorResponseDTO(String error, String message, Map<String, String> details) {
            this.error = error;
            this.message = message;
            this.timestamp = LocalDateTime.now();
            this.details = details;
        }

}
