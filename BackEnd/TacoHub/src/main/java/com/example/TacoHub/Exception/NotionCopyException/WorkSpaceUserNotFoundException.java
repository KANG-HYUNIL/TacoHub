package com.example.TacoHub.Exception.NotionCopyException;

public class WorkSpaceUserNotFoundException extends RuntimeException {

    public WorkSpaceUserNotFoundException(String message) {
        super(message);
    }

    public WorkSpaceUserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public WorkSpaceUserNotFoundException(Throwable cause) {
        super(cause);
    }
    
}
