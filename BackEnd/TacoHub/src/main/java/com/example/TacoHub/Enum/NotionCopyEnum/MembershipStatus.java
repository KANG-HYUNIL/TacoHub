package com.example.TacoHub.Enum.NotionCopyEnum;

public enum MembershipStatus {

    ACTIVE("ACTIVE"),
    INVITED("INVITED"),
    SUSPENDED("SUSPENDED");

    private final String description;
    MembershipStatus(String description)
    {
        this.description = description;
    }

}