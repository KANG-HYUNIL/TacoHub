package com.example.TacoHub.Exception;

public class InvalidLoginRequestException extends RuntimeException {
    public InvalidLoginRequestException(String message) {
        super(message);
    }

    public InvalidLoginRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidLoginRequestException(Throwable cause) {
        super(cause);
    }

}
