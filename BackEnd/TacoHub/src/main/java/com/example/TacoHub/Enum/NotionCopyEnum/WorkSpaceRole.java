package com.example.TacoHub.Enum.NotionCopyEnum;

public enum WorkSpaceRole {

    OWNER("OWNER"),
    ADMIN("ADMIN"),
    MEMBER("MEMBER"),
    GUEST("GUEST");

    private final String description;

    WorkSpaceRole(String description)
    {
        this.description = description;
    }

}


