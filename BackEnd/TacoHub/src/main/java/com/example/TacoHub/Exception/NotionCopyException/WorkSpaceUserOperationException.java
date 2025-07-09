package com.example.TacoHub.Exception.NotionCopyException;

public class WorkSpaceUserOperationException extends RuntimeException {

    public WorkSpaceUserOperationException(String message) {
        super(message);
    }

    public WorkSpaceUserOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public WorkSpaceUserOperationException(Throwable cause) {
        super(cause);
    }
    
}
