package com.example.TacoHub.Service.NotionCopyService;

import org.hibernate.boot.registry.classloading.spi.ClassLoaderService.Work;

import com.example.TacoHub.Entity.AccountEntity;
import com.example.TacoHub.Entity.NotionCopyEntity.WorkSpaceEntity;
import com.example.TacoHub.Entity.NotionCopyEntity.WorkSpaceUserEntity;
import com.example.TacoHub.Enum.NotionCopyEnum.MembershipStatus;
import com.example.TacoHub.Enum.NotionCopyEnum.WorkSpaceRole;

public class WorkSpaceUserFactory {

    /**
     * Role 이 OWNER인 WorkSpaceUserEntity를 생성하는 Factory Method
     * @param workspace 워크스페이스 엔티티
     * @param user 사용자 엔티티
     * @return 생성된 WorkSpaceUserEntity
     */
    public static WorkSpaceUserEntity createOwnerEntity(
            WorkSpaceEntity workspace, 
            AccountEntity user) 
    {
        
        return WorkSpaceUserEntity.builder()
                .workspace(workspace)
                .user(user)
                .workspaceRole(WorkSpaceRole.OWNER)
                .membershipStatus(MembershipStatus.ACTIVE)
                .build();
            
    }

    /**
     * Role을 Admin, Status를 Invited로 WorkSpaceUserEntity를 생성하는 Factory Method
     * @param workspace 워크스페이스 엔티티
     * @param user 사용자 엔티티
     * @return 생성된 WorkSpaceUserEntity
     */
    public static WorkSpaceUserEntity createdInvitedAdminEntity(
            WorkSpaceEntity workspace, 
            AccountEntity user) 
    {
        
        return WorkSpaceUserEntity.builder()
                .workspace(workspace)
                .user(user)
                .workspaceRole(WorkSpaceRole.ADMIN)
                .membershipStatus(MembershipStatus.INVITED)
                .build();
            
    }


        /**
     * Role을 Member, Status를 Invited로 WorkSpaceUserEntity를 생성하는 Factory Method
     * @param workspace 워크스페이스 엔티티
     * @param user 사용자 엔티티
     * @return 생성된 WorkSpaceUserEntity
     */
    public static WorkSpaceUserEntity createdInvitedMemberEntity(
            WorkSpaceEntity workspace, 
            AccountEntity user) 
    {
        
        return WorkSpaceUserEntity.builder()
                .workspace(workspace)
                .user(user)
                .workspaceRole(WorkSpaceRole.MEMBER)
                .membershipStatus(MembershipStatus.INVITED)
                .build();
            
    }


        /**
     * Role을 Guest, Status를 Invited로 WorkSpaceUserEntity를 생성하는 Factory Method
     * @param workspace 워크스페이스 엔티티
     * @param user 사용자 엔티티
     * @return 생성된 WorkSpaceUserEntity
     */
    public static WorkSpaceUserEntity createdInvitedGuestEntity(
            WorkSpaceEntity workspace, 
            AccountEntity user) 
    {
        
        return WorkSpaceUserEntity.builder()
                .workspace(workspace)
                .user(user)
                .workspaceRole(WorkSpaceRole.GUEST)
                .membershipStatus(MembershipStatus.INVITED)
                .build();
            
    }


        /**
     * Role을 Admin, Status를 Active로 WorkSpaceUserEntity를 생성하는 Factory Method
     * @param workspace 워크스페이스 엔티티
     * @param user 사용자 엔티티
     * @return 생성된 WorkSpaceUserEntity
     */
    public static WorkSpaceUserEntity createdActiveAdminEntity(
            WorkSpaceEntity workspace, 
            AccountEntity user) 
    {
        
        return WorkSpaceUserEntity.builder()
                .workspace(workspace)
                .user(user)
                .workspaceRole(WorkSpaceRole.ADMIN)
                .membershipStatus(MembershipStatus.ACTIVE)
                .build();
            
    }


            /**
     * Role을 Member, Status를 Active로 WorkSpaceUserEntity를 생성하는 Factory Method
     * @param workspace 워크스페이스 엔티티
     * @param user 사용자 엔티티
     * @return 생성된 WorkSpaceUserEntity
     */
    public static WorkSpaceUserEntity createdActiveMemberEntity(
            WorkSpaceEntity workspace, 
            AccountEntity user) 
    {
        
        return WorkSpaceUserEntity.builder()
                .workspace(workspace)
                .user(user)
                .workspaceRole(WorkSpaceRole.MEMBER)
                .membershipStatus(MembershipStatus.ACTIVE)
                .build();
            
    }
    


            /**
     * Role을 Guest, Status를 Active로 WorkSpaceUserEntity를 생성하는 Factory Method
     * @param workspace 워크스페이스 엔티티
     * @param user 사용자 엔티티
     * @return 생성된 WorkSpaceUserEntity
     */
    public static WorkSpaceUserEntity createdActiveGuestEntity(
            WorkSpaceEntity workspace, 
            AccountEntity user) 
    {
        
        return WorkSpaceUserEntity.builder()
                .workspace(workspace)
                .user(user)
                .workspaceRole(WorkSpaceRole.GUEST)
                .membershipStatus(MembershipStatus.ACTIVE)
                .build();
            
    }



            /**
     * R, Status를 Suspended로 WorkSpaceUserEntity를 생성하는 Factory Method
     * @param workspace 워크스페이스 엔티티
     * @param user 사용자 엔티티
     * @param workspaceRole 워크스페이스 역할 (예: ADMIN, MEMBER 등)
     * @return 생성된 WorkSpaceUserEntity
     */
    public static WorkSpaceUserEntity createdSuspendedEntity(
            WorkSpaceEntity workspace, 
            AccountEntity user,
            WorkSpaceRole workspaceRole) 
    {
        
        return WorkSpaceUserEntity.builder()
                .workspace(workspace)
                .user(user)
                .workspaceRole(workspaceRole)
                .membershipStatus(MembershipStatus.SUSPENDED)
                .build();
            
    }


}
