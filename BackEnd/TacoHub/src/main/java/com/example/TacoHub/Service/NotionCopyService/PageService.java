package com.example.TacoHub.Service.NotionCopyService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.example.TacoHub.Entity.NotionCopyEntity.PageEntity;
import com.example.TacoHub.Entity.NotionCopyEntity.WorkSpaceEntity;
import com.example.TacoHub.Exception.BusinessException;
import com.example.TacoHub.Exception.NotionCopyException.PageNotFoundException;
import com.example.TacoHub.Exception.NotionCopyException.PageOperationException;
import com.example.TacoHub.Exception.NotionCopyException.WorkSpaceNotFoundException;
import com.example.TacoHub.Exception.NotionCopyException.WorkSpaceOperationException;
import com.example.TacoHub.Logging.AuditLogging;
import com.example.TacoHub.Repository.NotionCopyRepository.PageRepository;
import com.example.TacoHub.Service.BaseService;

import jakarta.transaction.Transactional;

@Service
@Slf4j
public class PageService extends BaseService {

    private final PageRepository pageRepository;
    private final WorkSpaceService workSpaceService;
    private final BlockService blockService;

    public PageService (@Lazy WorkSpaceService workSpaceService,
                        @Lazy BlockService blockService,
                        PageRepository pageRepository) {
        this.workSpaceService = workSpaceService;
        this.blockService = blockService;
        this.pageRepository = pageRepository;
    }


    private final String newPageName = "New Page";
    private final Float defaultOrderIndex = 100f; // 기본 순서 인덱스

    /**
     * Workspace Root PageEntity를 생성하고 저장하는 메서드
     * @param workspaceId 워크스페이스 ID
     * @return PageEntity 생성된 페이지 엔티티
     * @throws WorkSpaceNotFoundException 워크스페이스가 존재하지 않을 경우
     */
    public PageEntity createPageEntity(UUID workspaceId) {
        return createPageEntity(workspaceId, null, null);
    }


    /** Workspace Root PageEntity를 생성하고 저장하는 메서드
     * @param workspaceId 워크스페이스 ID
     * @return PageEntity 생성된 페이지 엔티티
     * @throws WorkSpaceNotFoundException 워크스페이스가 존재하지 않을 경우
     */
    public PageEntity createPageEntity(UUID workspaceId, UUID parentPageId) {
        return createPageEntity(workspaceId, parentPageId, null);
    }


    /**
     * PageEntity를 생성하고 저장하는 메서드
     * @param workspaceId 워크스페이스 ID
     * @param parentPagId 부모 페이지 ID (null일 경우 최상위 페이지로 생성)
     * @return PageEntity 생성된 페이지 엔티티
     * @throws WorkSpaceNotFoundException 워크스페이스가 존재하지 않을 경우
     */
    @AuditLogging(action = "페이지_생성", includeParameters = true, includePerformance = true)
    @Transactional
    public PageEntity createPageEntity(UUID workspaceId, UUID parentPagId, Float orderIndex) {
   
        try {
            log.info("createPageEntity Start : workspaceId={}, parentPageId={}", workspaceId, parentPagId);

            // 의도된 비즈니스 예외 - 입력값 검증 (발생 지점 로깅)
            validateWorkspaceId(workspaceId);

            // 의도된 비즈니스 예외 - 워크스페이스 조회 (WorkSpaceService에서 로깅)
            WorkSpaceEntity workspace = workSpaceService.getWorkSpaceEntityOrThrow(workspaceId);

            // PageEntity 생성
            PageEntity newPage = PageEntity.builder()
                .title(newPageName)
                .workspace(workspace)
                .parentPage(parentPagId != null ? getPageEntityOrThrow(parentPagId) : null)
                .orderIndex(orderIndex != null ? orderIndex : defaultOrderIndex)
                .isRoot(parentPagId == null)
                .build();

            // 예상치 못한 예외 가능 영역 - 페이지 저장
            PageEntity savedPage = pageRepository.save(newPage);
            log.info("createPageEntity Success : pageId={}, title={}", savedPage.getId(), savedPage.getTitle());

            return savedPage;

        } catch (PageOperationException e) {
            // 1단계: 해당 메서드 자체 throw catch (구체적 예외)
            log.warn("페이지 생성 입력값 오류: 메서드=createPageEntity, workspaceId={}, parentPageId={}, 원인={}", 
                    workspaceId, parentPagId, e.getMessage());
            throw e;
        } catch (BusinessException e) {
            // 2단계: 상위 전파 의식한 비즈니스 catch (모든 비즈니스 예외)
            log.warn("페이지 생성 비즈니스 오류: 메서드=createPageEntity, workspaceId={}, parentPageId={}, 원인={}", 
                    workspaceId, parentPagId, e.getMessage());
            throw e;
        } catch (Exception e) {
            // 3단계: 시스템 예외 catch
            handleAndThrowPageException("createPageEntity", e);
            return null; // 실제로는 도달하지 않음
        }
    }


    /**
     * Workspace Id를 통해 PageEntity 삭제 하는 메서드
     * @param workspaceId 삭제할 워크스페이스 ID
     */
    @Transactional
    public void deletePageEntityByWorkspaceId(UUID workspaceId) {
        try {
            log.info("deletePageEntityByWorkspaceId Start : workspaceId={}", workspaceId);

            // 의도된 비즈니스 예외 - 입력값 검증 (발생 지점 로깅)
            validateWorkspaceId(workspaceId);

            // Workspace의 모든 page 가져오기
            List<PageEntity> pages = pageRepository.findAllByWorkspace_Id(workspaceId);

            // Page들에 대해 block 제거 시작
            for (PageEntity page : pages) {
                blockService.deleteBlockByPageId(page.getId());
            }

            // 해당 워크스페이스의 모든 페이지 삭제
            pageRepository.deleteByWorkspace_Id(workspaceId);
            log.info("deletePageEntityByWorkspaceId Success : workspaceId={}", workspaceId);

        } catch (PageOperationException e) {
            // 1단계: 해당 메서드 자체 throw catch (구체적 예외)
            log.warn("페이지 삭제 입력값 오류: 메서드=deletePageEntityByWorkspaceId, workspaceId={}, 원인={}", 
                    workspaceId, e.getMessage());
            throw e;
        } catch (BusinessException e) {
            // 2단계: 상위 전파 의식한 비즈니스 catch (모든 비즈니스 예외)
            log.warn("페이지 삭제 비즈니스 오류: 메서드=deletePageEntityByWorkspaceId, workspaceId={}, 원인={}", 
                    workspaceId, e.getMessage());
            throw e;
        } catch (Exception e) {
            // 3단계: 시스템 예외 catch
            handleAndThrowPageException("deletePageEntityByWorkspaceId", e);
        }
    }

    /**
     * Page Id를 통해 PageEntity 가져오기 시도 메서드
     * @param pageId 조회할 페이지 ID
     * @return PageEntity 검색 결과  
     * @throws PageNotFoundException 페이지가 존재하지 않을 경우
     */
    @AuditLogging(action = "페이지_조회", includeParameters = true)
    public PageEntity getPageEntityOrThrow(UUID pageId) {
        return pageRepository.findById(pageId)
                .orElseThrow(() -> {
                    log.warn("페이지 조회 실패: ID가 존재하지 않음, pageId={}", pageId);
                    return new PageNotFoundException("페이지가 존재하지 않습니다: " + pageId);
                });
    }

    /**
     * 페이지 제목을 변경하는 메서드
     * @param pageId 변경할 페이지 ID
     * @param newTitle 새로운 제목
     * @return PageEntity 업데이트된 페이지 엔티티
     * @throws PageNotFoundException 페이지가 존재하지 않을 경우
     * @throws PageOperationException 입력값 검증 실패 시
     */
    @Transactional
    public PageEntity updatePageTitle(UUID pageId, String newTitle) {
        String methodName = "updatePageTitle";
        
        try {
            log.info("[{}] 페이지 제목 변경 시작: pageId={}, newTitle={}", methodName, pageId, newTitle);

            // 의도된 비즈니스 예외 - 입력값 검증 (발생 지점 로깅)
            validatePageId(pageId);
            validatePageTitle(newTitle, methodName);

            // 의도된 비즈니스 예외 - 페이지 조회 (발생 지점 로깅)
            PageEntity page = getPageEntityOrThrow(pageId);
            
            String oldTitle = page.getTitle();
            
            // 제목 변경
            page.setTitle(newTitle);
            
            // 예상치 못한 예외 가능 영역 - 페이지 저장
            PageEntity updatedPage = pageRepository.save(page);
            
            log.info("[{}] 페이지 제목 변경 완료: pageId={}, oldTitle={}, newTitle={}", 
                    methodName, pageId, oldTitle, newTitle);
            
            return updatedPage;

        } catch (PageOperationException e) {
            // 1단계: 해당 메서드 자체 throw catch (구체적 예외)
            log.warn("[{}] 페이지 제목 변경 입력값 오류: pageId={}, newTitle={}, 원인={}", 
                    methodName, pageId, newTitle, e.getMessage());
            throw e;
        } catch (BusinessException e) {
            // 2단계: 상위 전파 의식한 비즈니스 catch (모든 비즈니스 예외)
            log.warn("[{}] 페이지 제목 변경 비즈니스 오류: pageId={}, newTitle={}, 원인={}", 
                    methodName, pageId, newTitle, e.getMessage());
            throw e;
        } catch (Exception e) {
            // 3단계: 시스템 예외 catch
            handleAndThrowPageException(methodName, e);
            return null; // 실제로는 도달하지 않음
        }
    }

    /**
     * 워크스페이스 ID 검증 메서드
     * @param workspaceId 검증할 워크스페이스 ID
     * @throws PageOperationException 워크스페이스 ID가 null일 경우
     */
    private void validateWorkspaceId(UUID workspaceId) {
        if (isNull(workspaceId)) {
            log.warn("워크스페이스 ID 검증 실패: ID가 null");
            throw new PageOperationException("워크스페이스 ID는 필수입니다");
        }
    }
    
    /**
     * 페이지 ID 검증 메서드
     * @param pageId 검증할 페이지 ID
     * @throws PageOperationException 페이지 ID가 null일 경우
     */
    private void validatePageId(UUID pageId) {
        if (isNull(pageId)) {
            log.warn("페이지 ID 검증 실패: ID가 null");
            throw new PageOperationException("페이지 ID는 필수입니다");
        }
    }

    /**
     * 페이지 제목 검증 메서드
     * @param title 검증할 페이지 제목
     * @param methodName 호출 메서드명
     * @throws PageOperationException 제목이 유효하지 않을 경우
     */
    private void validatePageTitle(String title, String methodName) {
        if (isStringNullOrEmpty(title)) {
            log.warn("페이지 제목 검증 실패: 메서드={}, 원인=제목이 null 또는 빈 문자열", methodName);
            throw new PageOperationException("페이지 제목은 필수입니다.");
        }
        
        if (isStringTooLong(title, 255)) {
            log.warn("페이지 제목 검증 실패: 메서드={}, 원인=제목이 너무 김, length={}", 
                    methodName, title.length());
            throw new PageOperationException("페이지 제목은 255자를 초과할 수 없습니다.");
        }
        
        // 특수문자 제한 (기본적인 검증)
        if (title.trim().isEmpty()) {
            log.warn("페이지 제목 검증 실패: 메서드={}, 원인=공백만으로 구성된 제목", methodName);
            throw new PageOperationException("페이지 제목은 공백만으로 구성될 수 없습니다.");
        }
    }

    /**
     * 공통 Page 예외 처리 메서드
     * 예외 타입에 따라 자동으로 warn/error 로깅을 결정
     * 
     * @param methodName 실패한 메서드명
     * @param originalException 원본 예외
     * @throws PageOperationException 래핑된 예외
     */
    private void handleAndThrowPageException(String methodName, Exception originalException) {
        PageOperationException customException = new PageOperationException(
            String.format("%s 실패 [%s]: %s", methodName, 
                         originalException.getClass().getSimpleName(), 
                         originalException.getMessage()),
            originalException
        );
        
        // BaseService의 메서드를 사용하여 예외 타입에 따라 warn/error 로깅
        handleAndThrow(methodName, originalException, customException);
    }


}
