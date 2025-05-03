package com.example.TacoHub.Exception;

public class InvalidAuthCodeException extends RuntimeException{

    public InvalidAuthCodeException(String message) {
        super(message);
    }

    public InvalidAuthCodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidAuthCodeException(Throwable cause) {
        super(cause);
    }

}
