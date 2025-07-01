package com.example.TacoHub.Exception.NotionCopyException;

public class WorkSpaceOperationException extends RuntimeException {

    public WorkSpaceOperationException(String message) {
        super(message);
    }

    public WorkSpaceOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public WorkSpaceOperationException(Throwable cause) {
        super(cause);
    }
}
