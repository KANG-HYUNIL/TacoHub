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


    public abstract boolean canManageWorkspace();
    public abstract boolean canInviteAndDeleteUsers();
    public abstract boolean canDeletePage();
    public abstract boolean canEditPage();
    public abstract boolean canViewPage();

}


