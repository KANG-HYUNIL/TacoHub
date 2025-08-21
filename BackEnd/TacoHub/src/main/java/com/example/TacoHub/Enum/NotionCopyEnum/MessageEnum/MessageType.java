package com.example.TacoHub.Enum.NotionCopyEnum.MessageEnum;

// 2. MessageType.java
public enum MessageType {
    BLOCK("block"),
    PAGE("page"), 
    WORKSPACE("workspace");
    
    private final String value;
    MessageType(String value) { this.value = value; }
    public String getValue() { return value; }
}
