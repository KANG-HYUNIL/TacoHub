package com.example.TacoHub.Enum.NotionCopyEnum.MessageEnum;

public enum BlockOperation {

    CREATE("create"),
    UPDATE("update"),
    DELETE("delete"),
    MOVE("move"),
    DUPLICATE("duplicate"),
    CONVERT_TYPE("convert_type"),
    INDENT("indent"),
    OUTDENT("outdent");
    
    
    private final String value;
    BlockOperation(String value) { this.value = value; }
    public String getValue() { return value; }
}