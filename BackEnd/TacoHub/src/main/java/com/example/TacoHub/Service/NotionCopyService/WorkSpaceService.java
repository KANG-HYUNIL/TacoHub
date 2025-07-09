package com.example.TacoHub.Service.NotionCopyService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.TacoHub.Converter.NotionCopyConveter.WorkSpaceConverter;
import com.example.TacoHub.Dto.NotionCopyDTO.WorkSpaceDTO;
import com.example.TacoHub.Entity.NotionCopyEntity.PageEntity;
import com.example.TacoHub.Entity.NotionCopyEntity.WorkSpaceEntity;
import com.example.TacoHub.Exception.NotionCopyException.WorkSpaceNotFoundException;
import com.example.TacoHub.Exception.NotionCopyException.WorkSpaceOperationException;
import com.example.TacoHub.Logging.UserInfoExtractor;
import com.example.TacoHub.Repository.NotionCopyRepository.WorkSpaceRepository;

import jakarta.transaction.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkSpaceService {

    private final WorkSpaceRepository workspaceRepository;
    private final PageService pageService;
    private final WorkSpaceUserService workSpaceUserService;
    private final UserInfoExtractor userInfoExtractor;

    /**
     * 새로운 워크스페이스를 생성합니다
     * @param newWorkspaceName 생성할 워크스페이스의 이름
     * @return 생성된 워크스페이스의 DTO
     */
    @Transactional
    public WorkSpaceEntity createWorkspaceEntity(String newWorkspaceName) {
        try {
            log.info("워크스페이스 생성 시작: name={}", newWorkspaceName);
            
            // 입력값 검증
            if (newWorkspaceName == null || newWorkspaceName.trim().isEmpty()) {
                log.warn("워크스페이스 생성 실패: 이름이 비어있음");
                throw new WorkSpaceOperationException("워크스페이스 이름은 필수입니다");
            }
            if (newWorkspaceName.length() > 100) {
                log.warn("워크스페이스 생성 실패: 이름이 너무 긺, length={}", newWorkspaceName.length());
                throw new WorkSpaceOperationException("워크스페이스 이름은 100자를 초과할 수 없습니다");
            }

            // 기초 entity 생성
            WorkSpaceEntity newWorkSpace = WorkSpaceEntity.builder()
                .name(newWorkspaceName.trim())
                .build();

            WorkSpaceEntity savedEntity = workspaceRepository.save(newWorkSpace);

            // 초기 default 페이지 생성
            PageEntity defaultRootPage = pageService.createPageEntity(savedEntity.getId());

            // 워크스페이스와 페이지 연관 설정
            savedEntity.getRootPages().add(defaultRootPage);
            WorkSpaceEntity updatedEntity = workspaceRepository.save(savedEntity);

            // 워크스페이스 사용자 생성 (관리자)
            String adminEmail = userInfoExtractor.getCurrentUserEmail();
            if (adminEmail == null || adminEmail.trim().isEmpty()) {
                log.warn("워크스페이스 생성 실패: 관리자 이메일이 비어있음");
                throw new WorkSpaceOperationException("관리자 이메일은 필수입니다");
            }

            workSpaceUserService.createAdminUserEntity(adminEmail, updatedEntity.getId());


            log.info("워크스페이스 생성 완료: id={}, name={}", updatedEntity.getId(), updatedEntity.getName());
            return updatedEntity;

        } catch (WorkSpaceNotFoundException | WorkSpaceOperationException e) {
            // 의도된 비즈니스 예외는 그대로 전파
            log.warn("워크스페이스 생성 비즈니스 오류: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            // 예상치 못한 예외는 래핑해서 전파
            log.error("워크스페이스 생성 시스템 오류: {}", e.getMessage(), e);
            handleAndThrowWorkSpaceException("createWorkspace", e);
            return null; // 실제로는 도달하지 않음
        }
    }

    /**
     * 워크스페이스의 이름을 변경합니다
     * @param newWorkspaceName 새로운 워크스페이스 이름
     * @param workspaceId 변경할 워크스페이스의 ID
     */
    @Transactional
    public void editWorkspaceName(String newWorkspaceName, UUID workspaceId) {
        try {
            log.info("워크스페이스 이름 변경 시작: id={}, newName={}", workspaceId, newWorkspaceName);
            
            // 입력값 검증
            if (workspaceId == null) {
                throw new WorkSpaceOperationException("워크스페이스 ID는 필수입니다");
            }
            if (newWorkspaceName == null || newWorkspaceName.trim().isEmpty()) {
                throw new WorkSpaceOperationException("워크스페이스 이름은 필수입니다");
            }
            if (newWorkspaceName.length() > 100) {
                throw new WorkSpaceOperationException("워크스페이스 이름은 100자를 초과할 수 없습니다");
            }

            // 워크스페이스 조회 (존재하지 않으면 예외 발생)
            WorkSpaceEntity workspace = getWorkSpaceEntityOrThrow(workspaceId);

            // workspace 이름 변경
            workspace.setName(newWorkspaceName.trim());

            // 변경된 워크스페이스 저장
            workspaceRepository.save(workspace);
            
            log.info("워크스페이스 이름 변경 완료: id={}, newName={}", workspaceId, newWorkspaceName);

        } catch (WorkSpaceNotFoundException | WorkSpaceOperationException e) {
            // 의도된 비즈니스 예외는 그대로 전파
            throw e;
        } catch (Exception e) {
            // 예상치 못한 예외는 래핑해서 전파
            handleAndThrowWorkSpaceException("editWorkspaceName", e);
        }
    }

    /**
     * 워크스페이스를 삭제합니다
     * @param workspaceId 삭제할 워크스페이스의 ID
     */
    @Transactional
    public void deleteWorkspace(UUID workspaceId) {
        try {
            log.info("워크스페이스 삭제 시작: id={}", workspaceId);
            
            // 입력값 검증
            if (workspaceId == null) {
                throw new WorkSpaceOperationException("워크스페이스 ID는 필수입니다");
            }

            // 워크스페이스 존재 확인 (존재하지 않으면 예외 발생)
            if (!workspaceRepository.existsById(workspaceId)) {
                throw new WorkSpaceNotFoundException("워크스페이스가 존재하지 않습니다: " + workspaceId);
            }

            // 삭제 권한 검증
            String currentUserEmail = userInfoExtractor.getCurrentUserEmail();
            if (currentUserEmail == null || currentUserEmail.trim().isEmpty()) {
                throw new WorkSpaceOperationException("현재 사용자 이메일을 확인할 수 없습니다");
            }

            // 워크스페이스 사용자 조회 (관리자 권한 확인)
            if (!workSpaceUserService.canUserManageWorkSpace(currentUserEmail, workspaceId)) {
                throw new WorkSpaceOperationException("워크스페이스 삭제 권한이 없습니다: " + currentUserEmail);
            }

            // TODO : 워크스페이스에 속한 페이지 삭제
            pageService.deletePageEntityByWorkspaceId(workspaceId);

            // 워크스페이스 - 사용자 관계 삭제
            workSpaceUserService.deleteWorkSpaceUserAllEntites(workspaceId);

            // 워크스페이스 삭제
            workspaceRepository.deleteById(workspaceId);
            
            log.info("워크스페이스 삭제 완료: id={}", workspaceId);

        } catch (WorkSpaceNotFoundException | WorkSpaceOperationException e) {
            // 의도된 비즈니스 예외는 그대로 전파
            throw e;
        } catch (Exception e) {
            // 예상치 못한 예외는 래핑해서 전파
            handleAndThrowWorkSpaceException("deleteWorkspace", e);
        }
    }

    /**
     * 워크스페이스 정보를 조회합니다
     * @param workspaceId 조회할 워크스페이스의 ID
     * @return 조회된 워크스페이스의 DTO
     */
    public WorkSpaceDTO getWorkspaceDto(UUID workspaceId) {
        try {
            log.info("워크스페이스 조회 시작: id={}", workspaceId);
            
            // 입력값 검증
            if (workspaceId == null) {
                throw new WorkSpaceOperationException("워크스페이스 ID는 필수입니다");
            }

            // 워크스페이스 조회 (존재하지 않으면 예외 발생)
            WorkSpaceEntity workspace = getWorkSpaceEntityOrThrow(workspaceId);

            WorkSpaceDTO dto = WorkSpaceConverter.toDTO(workspace);

            log.info("워크스페이스 조회 완료: id={}", workspaceId);
            return dto;

        } catch (WorkSpaceNotFoundException | WorkSpaceOperationException e) {
            // 의도된 비즈니스 예외는 그대로 전파
            throw e;
        } catch (Exception e) {
            // 예상치 못한 예외는 래핑해서 전파
            handleAndThrowWorkSpaceException("getWorkspace", e);
            return null; // 실제로는 도달하지 않음
        }
    }

    /**
     * WorkSpaceEntity를 ID로 조회하고, 없으면 예외를 던지는 메서드
     * @param workspaceId 조회할 워크스페이스의 Id
     * @return 조회된 WorkSpaceEntity
     * @throws WorkSpaceNotFoundException 워크스페이스가 존재하지 않을 때
     */
    public WorkSpaceEntity getWorkSpaceEntityOrThrow(UUID workspaceId) {
        return workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> {
                    log.warn("워크스페이스 조회 실패: ID가 존재하지 않음, id={}", workspaceId);
                    return new WorkSpaceNotFoundException("워크스페이스가 존재하지 않습니다: " + workspaceId);
                });
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
