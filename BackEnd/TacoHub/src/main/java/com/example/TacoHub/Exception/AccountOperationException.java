package com.example.TacoHub.Exception;

public class AccountOperationException extends RuntimeException {
    
    public AccountOperationException(String message) {
        super(message);
    }

    public AccountOperationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public AccountOperationException(Throwable cause) {
        super(cause);
    }
    
}
