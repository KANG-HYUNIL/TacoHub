package com.example.TacoHub.Exception;

public class AuthCodeOperationException extends RuntimeException {
    
    public AuthCodeOperationException(String message) {
        super(message);
    }

    public AuthCodeOperationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public AuthCodeOperationException(Throwable cause) {
        super(cause);
    }
    
}
