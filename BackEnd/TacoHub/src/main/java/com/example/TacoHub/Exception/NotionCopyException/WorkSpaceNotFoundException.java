package com.example.TacoHub.Exception.NotionCopyException;

public class WorkSpaceNotFoundException extends RuntimeException{

    public WorkSpaceNotFoundException(String message) {
        super(message);
    }

    public WorkSpaceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public WorkSpaceNotFoundException(Throwable cause) {
        super(cause);
    }
}
