package com.example.TacoHub.Enum.NotionCopyEnum;

public enum WorkSpaceRole {

    OWNER("OWNER")
    {
        @Override
        public boolean canManageWorkspace() {
            return true;
        }

        @Override
        public boolean canInviteAndDeleteUsers() {
            return true;
        }

        @Override
        public boolean canDeletePage(){
            return true;
        }

        @Override
        public boolean canEditPage() {
            return true;
        }

        @Override
        public boolean canViewPage() {
            return true;
        }
    },
    ADMIN("ADMIN")
    {
        @Override
        public boolean canManageWorkspace() {
            return true;
        }

        @Override
        public boolean canInviteAndDeleteUsers() {
            return true;
        }

        @Override
        public boolean canDeletePage(){
            return true;
        }

        @Override
        public boolean canEditPage() {
            return true;
        }

        @Override   
        public boolean canViewPage() {
            return true;
        }
    },
    MEMBER("MEMBER")    
    {
        @Override
        public boolean canManageWorkspace() {
            return false;
        }

        @Override
        public boolean canInviteAndDeleteUsers() {
            return false;
        }

        @Override
        public boolean canDeletePage(){
            return true;
        }

        @Override
        public boolean canEditPage() {
            return true;
        }

        @Override   
        public boolean canViewPage() {
            return true;
        }
    },
    GUEST("GUEST")
    {
        @Override
        public boolean canManageWorkspace() {
            return false;
        }

        @Override
        public boolean canInviteAndDeleteUsers() {
            return false;
        }

        @Override
        public boolean canDeletePage(){
            return false;
        }

        @Override
        public boolean canEditPage() {
            return false;
        }

        @Override   
        public boolean canViewPage() {
            return true;
        }
    };

    private final String description;

    WorkSpaceRole(String description)
    {
        this.description = description;
    }

    /**
     * 문자열을 WorkSpaceRole로 안전하게 변환
     * @param roleString 역할 문자열 (대소문자 무관)
     * @return WorkSpaceRole 또는 null (유효하지 않은 경우)
     */
    public static WorkSpaceRole fromString(String roleString) {
        if (roleString == null || roleString.trim().isEmpty()) {
            return null;
        }
        
        try {
            return WorkSpaceRole.valueOf(roleString.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * 문자열을 WorkSpaceRole로 변환 (예외 발생)
     * @param roleString 역할 문자열
     * @return WorkSpaceRole
     * @throws IllegalArgumentException 유효하지 않은 역할인 경우
     */
    public static WorkSpaceRole fromStringOrThrow(String roleString) {
        WorkSpaceRole role = fromString(roleString);
        if (role == null) {
            throw new IllegalArgumentException("유효하지 않은 워크스페이스 역할입니다: " + roleString);
        }
        return role;
    }

    /**
     * 유효한 역할 문자열인지 확인
     * @param roleString 확인할 문자열
     * @return 유효 여부
     */
    public static boolean isValidRole(String roleString) {
        return fromString(roleString) != null;
    }

    /**
     * Enum을 문자열로 변환
     * @return 역할 문자열
     */
    @Override
    public String toString() {
        return this.name();
    }

    /**
     * 설명 반환
     * @return 역할 설명
     */
    public String getDescription() {
        return description;
    }


    public abstract boolean canManageWorkspace();
    public abstract boolean canInviteAndDeleteUsers();
    public abstract boolean canDeletePage();
    public abstract boolean canEditPage();
    public abstract boolean canViewPage();

}


