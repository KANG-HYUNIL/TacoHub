package com.example.TacoHub.Service.NotionCopyService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.TacoHub.Entity.NotionCopyEntity.WorkSpaceUserEntity;
import com.example.TacoHub.Enum.NotionCopyEnum.MembershipStatus;
import com.example.TacoHub.Repository.NotionCopyRepository.WorkSpaceUserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkSpaceUserService {

    // User-Workspace 관계 담은 WorkSpaceUserRepository 종속성성
    private final WorkSpaceUserRepository workSpaceUserRepository;



    private Optional<WorkSpaceUserEntity> getWorkSpaceUserEntity(String userEmailId, UUID workspaceId)
    {
        return workSpaceUserRepository.findByUser_EmailIdAndWorkspace_Id(userEmailId, workspaceId);
    }



    // Check if a user can manage a workspace
    public boolean canUserManageWorkSpace(String userEmailId, UUID workspaceId)
    {
        try{

            Optional<WorkSpaceUserEntity> result =getWorkSpaceUserEntity(userEmailId, workspaceId);
            
            // row 존재 여부 확인
            if (result.isEmpty())
            {
                log.warn("");
                return false;
            }

            boolean role = result.filter(entity -> entity.getMembershipStatus() == MembershipStatus.ACTIVE)
                                .map(entity -> entity.getWorkspaceRole().canManageWorkspace())
                                .orElse(false);
            
            return role;
        } 
        catch(Exception e)
        {

            return false;
        }
    }

    // Check if a user can invite and delete users in a workspace
    public boolean canUserInviteAndDeleteUsers(String userEmailId, UUID workspaceId)
    {
                try{

            Optional<WorkSpaceUserEntity> result =getWorkSpaceUserEntity(userEmailId, workspaceId);
            
            // row 존재 여부 확인
            if (result.isEmpty())
            {
                log.warn("");
                return false;
            }

            boolean role = result.filter(entity -> entity.getMembershipStatus() == MembershipStatus.ACTIVE)
                                .map(entity -> entity.getWorkspaceRole().canInviteAndDeleteUsers())
                                .orElse(false);
            
            return role;
        } 
        catch(Exception e)
        {

            return false;
        }
    }

    
    // Check if a user can delete a page in a workspace 
    public boolean canUserDeletePage(String userEmailId, UUID workspaceId)
    {
                try{

            Optional<WorkSpaceUserEntity> result =getWorkSpaceUserEntity(userEmailId, workspaceId);
            
            // row 존재 여부 확인
            if (result.isEmpty())
            {
                log.warn("");
                return false;
            }

            boolean role = result.filter(entity -> entity.getMembershipStatus() == MembershipStatus.ACTIVE)
                                .map(entity -> entity.getWorkspaceRole().canDeletePage())
                                .orElse(false);
            
            return role;
        } 
        catch(Exception e)
        {

            return false;
        }
    }

    // Check if a user can edit or create page in workspcae
    public boolean canUserEditPage(String userEmailId, UUID workspaceId)
    {
                try{

            Optional<WorkSpaceUserEntity> result =getWorkSpaceUserEntity(userEmailId, workspaceId);
            
            // row 존재 여부 확인
            if (result.isEmpty())
            {
                log.warn("");
                return false;
            }

            boolean role = result.filter(entity -> entity.getMembershipStatus() == MembershipStatus.ACTIVE)
                                .map(entity -> entity.getWorkspaceRole().canEditPage())
                                .orElse(false);
            
            return role;
        } 
        catch(Exception e)
        {

            return false;
        }
    }

    // Check if a user can view page in workspace
    public boolean canUserViewPage(String userEmailId, UUID workspaceId)
    {
        try{

            Optional<WorkSpaceUserEntity> result =getWorkSpaceUserEntity(userEmailId, workspaceId);
            
            // row 존재 여부 확인
            if (result.isEmpty())
            {
                log.warn("");
                return false;
            }

            boolean role = result.filter(entity -> entity.getMembershipStatus() == MembershipStatus.ACTIVE)
                                .map(entity -> entity.getWorkspaceRole().canViewPage())
                                .orElse(false);
            
            return role;
        } 
        catch(Exception e)
        {

            return false;
        }
    }

}

