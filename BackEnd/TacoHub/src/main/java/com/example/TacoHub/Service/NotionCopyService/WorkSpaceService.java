package com.example.TacoHub.Service.NotionCopyService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.TacoHub.Converter.NotionCopyConveter.WorkSpaceConverter;
import com.example.TacoHub.Dto.NotionCopyDTO.WorkSpaceDTO;
import com.example.TacoHub.Entity.NotionCopyEntity.WorkSpaceEntity;
import com.example.TacoHub.Exception.NotionCopyException.WorkSpaceNotFoundException;
import com.example.TacoHub.Exception.NotionCopyException.WorkSpaceOperationException;
import com.example.TacoHub.Repository.NotionCopyRepository.WorkSpaceRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkSpaceService {

    private final WorkSpaceRepository workspaceRepository;

    /**
     * 새로운 워크스페이스를 생성합니다
     * @param newWorkspaceName 생성할 워크스페이스의 이름
     * @return 생성된 워크스페이스의 DTO
     */
    public WorkSpaceDTO createWorkspace(String newWorkspaceName)
    {
        try 
        {   
            // 기초 entity 생성
            WorkSpaceEntity newWorkSpace = WorkSpaceEntity.builder()
            .name(newWorkspaceName)
            .build();

            //이후 pageservice 완성해서 초기 default 생성해 넣어줄거임
            

            WorkSpaceEntity savedEntity = workspaceRepository.save(newWorkSpace);


            WorkSpaceDTO dto = WorkSpaceConverter.toDTO(savedEntity);

            return dto;

        } catch(Exception e)
        {
            handleAndThrowWorkSpaceException("createWorkspace", e);
            return null; // 실제로는 도달하지 않음
        }
    }

    /**
     * 워크스페이스의 이름을 변경합니다
     * @param newWorkspaceName 새로운 워크스페이스 이름
     * @param workspaceId 변경할 워크스페이스의 ID
     */
    public void editWorkspaceName(String newWorkspaceName, UUID workspaceId)
    {
        try{
            // 워크스페이스 조회
            Optional<WorkSpaceEntity> optionalWorkspace = workspaceRepository.findById(workspaceId);

            if (optionalWorkspace.isEmpty())
            {
                log.warn("editWorkspaceName 실패: ID가 존재하지 않음, id={}", workspaceId);
                throw new WorkSpaceNotFoundException("워크스페이스가 존재하지 않습니다.");
            }

            // 이름 변경
            WorkSpaceEntity workspace = optionalWorkspace.get();
            workspace.setName(newWorkspaceName);

            // 변경된 워크스페이스 저장
            workspaceRepository.save(workspace);            
            log.info("editWorkspaceName 성공: id={}, newName={}", workspaceId, newWorkspaceName);
            

        } catch (Exception e)
        {
            handleAndThrowWorkSpaceException("editWorkspaceName", e);
        }
    }

    /**
     * 워크스페이스를 삭제합니다
     * @param workspaceId 삭제할 워크스페이스의 ID
     */
    public void deleteWorkspace(UUID workspaceId)
    {
        try{

            // 워크스페이스 조회
            boolean isWorkspaceExists = workspaceRepository.existsById(workspaceId);

            if (!isWorkspaceExists)
            {
                log.warn("deleteWorkspace 실패: ID가 존재하지 않음, id={}", workspaceId);
                throw new WorkSpaceNotFoundException("워크스페이스가 존재하지 않습니다.");
            }

            workspaceRepository.deleteById(workspaceId);
            log.info("deleteWorkspace 성공: id={}", workspaceId);

        } catch (Exception e)
        {
            handleAndThrowWorkSpaceException("deleteWorkspace", e);
        }

    }

    /**
     * 워크스페이스 정보를 조회합니다
     * @param workspaceId 조회할 워크스페이스의 ID
     * @return 조회된 워크스페이스의 DTO
     */
    public WorkSpaceDTO getWorkspace(UUID workspaceId)
    {
        try 
        {   
            Optional<WorkSpaceEntity> workspace = workspaceRepository.findById(workspaceId);

            if (workspace.isEmpty())
            {
                log.warn("getWorkspace 실패: ID가 존재하지 않음, id={}", workspaceId);
                throw new WorkSpaceNotFoundException("워크스페이스가 존재하지 않습니다.");
            }

            WorkSpaceDTO dto = WorkSpaceConverter.toDTO(workspace.get());

            return dto;

        } catch(Exception e)
        {
            handleAndThrowWorkSpaceException("getWorkspace", e);
            return null; // 실제로는 도달하지 않음
        } 
        
    }

    /**
     * 공통 WorkSpace 예외 처리 메서드
     * @param methodName 실패한 메서드명
     * @param originalException 원본 예외
     * @throws WorkSpaceOperationException 래핑된 예외
     */
    private void handleAndThrowWorkSpaceException(String methodName, Exception originalException) {
        String errorMessage = originalException.getMessage();
        String exceptionType = originalException.getClass().getSimpleName();
        
        log.error("{} 실패: type={}, message={}", methodName, exceptionType, errorMessage, originalException);
        
        throw new WorkSpaceOperationException(
            String.format("%s 실패 [%s]: %s", methodName, exceptionType, errorMessage),
            originalException
        );
    }



}
