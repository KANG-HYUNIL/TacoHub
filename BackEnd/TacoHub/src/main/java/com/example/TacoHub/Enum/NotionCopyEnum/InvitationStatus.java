package com.example.TacoHub.Enum.NotionCopyEnum;

/**
 * 초대 상태를 나타내는 열거형
 */
public enum InvitationStatus {
    
    /**
     * 대기 중 - 초대가 생성되었지만 아직 수락되지 않은 상태
     */
    PENDING("대기 중"),
    
    /**
     * 수락됨 - 초대가 수락되어 워크스페이스 멤버가 된 상태
     */
    ACCEPTED("수락됨"),
    
    /**
     * 만료됨 - 초대 기간이 만료된 상태
     */
    EXPIRED("만료됨"),
    
    /**
     * 취소됨 - 초대한 사람이 취소한 상태
     */
    CANCELLED("취소됨");
    
    private final String description;
    
    InvitationStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
