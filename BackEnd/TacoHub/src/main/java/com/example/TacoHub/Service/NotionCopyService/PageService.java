package com.example.TacoHub.Service.NotionCopyService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

import com.example.TacoHub.Entity.NotionCopyEntity.PageEntity;
import com.example.TacoHub.Entity.NotionCopyEntity.WorkSpaceEntity;
import com.example.TacoHub.Exception.NotionCopyException.PageNotFoundException;
import com.example.TacoHub.Exception.NotionCopyException.PageOperationException;
import com.example.TacoHub.Exception.NotionCopyException.WorkSpaceNotFoundException;
import com.example.TacoHub.Exception.NotionCopyException.WorkSpaceOperationException;
import com.example.TacoHub.Repository.NotionCopyRepository.PageRepository;

import jakarta.transaction.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PageService {

    private final PageRepository pageRepository;
    private final WorkSpaceService workSpaceService;
    private final BlockService blockService;


    private final String newPageName = "New Page";
    private final Float defaultOrderIndex = 100f; // 기본 순서 인덱스

    /**
     * Workspace Root PageEntity를 생성하고 저장하는 메서드
     * @param workspaceId 워크스페이스 ID
     * @return PageEntity 생성된 페이지 엔티티
     * @throws WorkSpaceNotFoundException 워크스페이스가 존재하지 않을 경우
     */
    public PageEntity createPageEntity(UUID workspaceId) {
        return createPageEntity(workspaceId, null);
    }


    /**
     * PageEntity를 생성하고 저장하는 메서드
     * @param workspaceId 워크스페이스 ID
     * @param parentPagId 부모 페이지 ID (null일 경우 최상위 페이지로 생성)
     * @return PageEntity 생성된 페이지 엔티티
     * @throws WorkSpaceNotFoundException 워크스페이스가 존재하지 않을 경우
     */
    @Transactional
    public PageEntity createPageEntity(UUID workspaceId, UUID parentPagId) {
   
        try {

            log.info("createPageEntity Start : workspaceId={}, parentPageId={}", workspaceId, parentPagId);

            // 입력값 검증
            if (workspaceId == null) {
                log.warn("워크스페이스 ID가 null");
                throw new PageOperationException("워크스페이스 ID는 필수입니다");
            }

            // 워크스페이스 조회, workspace 없으면 메서드 측에서 error throw
            WorkSpaceEntity workspace = workSpaceService.getWorkSpaceEntityOrThrow(workspaceId);


            //PageEntity 생성
            PageEntity newPage = PageEntity.builder()
                .title(newPageName)
                .workspace(workspace) // 워크스페이스 설정
                .parentPage(parentPagId != null ? getPageEntityOrThrow(parentPagId) : null)
                .orderIndex(defaultOrderIndex) // 기본 순서 인덱스 = 100f
                .isRoot(parentPagId == null) // 부모 페이지가 없으면 루트 페이지
                .build();

            // 페이지 저장
            PageEntity savedPage = pageRepository.save(newPage);
            log.info("createPageEntity Success : pageId={}, title={}", savedPage.getId(), savedPage.getTitle());

            // Page return
            return savedPage;

        } catch (WorkSpaceNotFoundException | WorkSpaceOperationException e)
        {
            // 의도된 비즈니스 에러는 상위로 전파
            log.warn("워크스페이스 조회 비즈니스 오류: workspaceId={}, 원인={}", workspaceId, e.getMessage());
            throw e;
        }
        catch (PageOperationException e)
        {
            // 페이지 관련 비즈니스 예외는 그대로 전파
            log.warn("페이지 생성 비즈니스 오류: workspaceId={}, parentPageId={}, 원인={}", 
                    workspaceId, parentPagId, e.getMessage());
            throw e;
        }
        catch (Exception e) {
            // 기타 예외는 공통 예외 처리 메서드로 처리
            handleAndThrowPageException("createPageEntity", e);
            return null;
        }


    }


    /**
     * Workspace Id를 통해 PageEntity 삭제 하는 메서드
     * @param workspaceId
     * @return
     */
    @Transactional
    public void deletePageEntityByWorkspaceId(UUID workspaceId) {
        try {
            log.info("deletePageEntityByWorkspaceId Start : workspaceId={}", workspaceId);

            // 입력값 검증
            if (workspaceId == null) {
                log.warn("워크스페이스 ID가 null");
                throw new PageOperationException("워크스페이스 ID는 필수입니다");
            }

            // Workspace의 모든 page 가져와야 함
            List<PageEntity> pages = pageRepository.findAllByWorkspace_Id(workspaceId);

            // Page들에 대해 block 제거 시작
            for (PageEntity page : pages) {
                // TODO : Page의 모든 Block 제거
                blockService.deleteBlockByPageId(page.getId());
            }

            // 해당 워크스페이스의 모든 페이지 삭제
            pageRepository.deleteByWorkspace_Id(workspaceId);
            log.info("deletePageEntityByWorkspaceId Success : workspaceId={}", workspaceId);

        } 
        catch (WorkSpaceNotFoundException | WorkSpaceOperationException e) 
        {
            // 의도된 비즈니스 예외는 그대로 전파
            log.warn("워크스페이스 조회 비즈니스 오류: workspaceId={}, 원인={}", workspaceId, e.getMessage());
            throw e;
        } 
        catch (Exception e) 
        {
            // 기타 예외는 공통 예외 처리 메서드로 처리
            handleAndThrowPageException("deletePageEntityByWorkspaceId", e);
        }
    }

    /**
     * Page Id를 통해 PageEntity 가져오기 시도 메서드
     * @param pageId 조회할 페이지 ID
     * @return PageEntity 검색 결과  
     * @throws PageNotFoundException 페이지가 존재하지 않을 경우
     */
    public PageEntity getPageEntityOrThrow(UUID pageId) {
        return pageRepository.findById(pageId)
                .orElseThrow(() -> {
                    log.warn("페이지 조회 실패: ID가 존재하지 않음, pageId={}", pageId);
                    return new PageNotFoundException("페이지가 존재하지 않습니다: " + pageId);
                });
    }


    /**
     * 공통 Page 예외 처리 메서드
     * @param methodName 실패한 메서드명
     * @param originalException 원본 예외
     * @throws PageOperationException 래핑된 예외
     */
    private void handleAndThrowPageException(String methodName, Exception originalException) {
        String errorMessage = originalException.getMessage();
        String exceptionType = originalException.getClass().getSimpleName();
        
        log.error("{} 실패: type={}, message={}", methodName, exceptionType, errorMessage, originalException);
        
        throw new PageOperationException(
            String.format("%s 실패 [%s]: %s", methodName, exceptionType, errorMessage),
            originalException
        );
    }


}
