package com.example.TacoHub.Exception;

public class EmailOperationException extends RuntimeException {
    
    public EmailOperationException(String message) {
        super(message);
    }

    public EmailOperationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public EmailOperationException(Throwable cause) {
        super(cause);
    }
    
}
