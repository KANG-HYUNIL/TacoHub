package com.example.TacoHub.Service.NotionCopyService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;
import org.springframework.stereotype.Service;

import com.example.TacoHub.Converter.NotionCopyConveter.PageConverter;
import com.example.TacoHub.Dto.NotionCopyDTO.PageDTO;
import com.example.TacoHub.Entity.NotionCopyEntity.PageEntity;
import com.example.TacoHub.Entity.NotionCopyEntity.WorkSpaceEntity;
import com.example.TacoHub.Exception.NotionCopyException.PageNotFoundException;
import com.example.TacoHub.Exception.NotionCopyException.PageOperationException;
import com.example.TacoHub.Exception.NotionCopyException.WorkSpaceNotFoundException;
import com.example.TacoHub.Repository.NotionCopyRepository.PageRepository;
import com.example.TacoHub.Repository.NotionCopyRepository.WorkSpaceRepository;

import jakarta.transaction.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PageService {

    private final PageRepository pageRepository;
    private final BlockService blockService; // TODO: 블록 기능 구현 시 활성화
    private final WorkSpaceRepository workspaceRepository;

    private final String newPageName = "New Page";

    /**
     * 기존 페이지 내용을 복사하여 새로운 페이지를 만듭니다
     * @param pageId 복사할 페이지의 ID
     * @param workspaceId 대상 워크스페이스 ID
     * @param parentPageId 부모 페이지 ID (null일 경우 루트 페이지)
     */
    public void copyPage(UUID pageId, UUID workspaceId, UUID parentPageId)
    {

    }

    /**
     * 새로운 페이지를 생성합니다
     * @param workspaceId 페이지를 생성할 워크스페이스 ID
     * @param parentPageId 부모 페이지 ID (null일 경우 루트 페이지)
     * @return 생성된 페이지의 Entity
     */
    @Transactional
    public PageEntity createPageEntity(UUID workspaceId, UUID parentPageId) {
        try {
            log.info("페이지 생성 시작: workspaceId={}, parentPageId={}", workspaceId, parentPageId);
            
            // 입력값 검증
            if (workspaceId == null) {
                throw new PageOperationException("워크스페이스 ID는 필수입니다");
            }

            // 기반 workspace 존재 검증
            WorkSpaceEntity workspace = workspaceRepository.findById(workspaceId)
                    .orElseThrow(() -> new WorkSpaceNotFoundException("워크스페이스가 존재하지 않습니다: " + workspaceId));

            // 부모 page 존재 검증 (parentPageId가 있는 경우)
            PageEntity parentPage = null;
            if (parentPageId != null) {
                parentPage = getPageEntityOrThrow(parentPageId);
                
                // 부모 페이지가 같은 워크스페이스에 속하는지 확인
                if (!parentPage.getWorkspace().getId().equals(workspaceId)) {
                    throw new PageOperationException("부모 페이지와 같은 워크스페이스에만 자식 페이지를 생성할 수 있습니다");
                }
            }

            // 새 PageEntity 생성
            PageEntity newPage = PageEntity.builder()
                    .title(newPageName)
                    .workspace(workspace)
                    .parentPage(parentPage)
                    .isRoot(parentPage == null)
                    .orderIndex(0) // TODO: 적절한 순서 인덱스 계산 로직 추가
                    .build();

            // page 저장
            PageEntity savedPage = pageRepository.save(newPage);

            // 부모가 있는 page면 부모 page에 새 page 추가
            if (parentPage != null) {
                addChildPage(parentPage, savedPage);
                // 📄 비즈니스 이벤트: 자식 페이지 생성
                log.info("자식 페이지 생성 완료: pageId={}, parentPageId={}, workspaceId={}, title={}", 
                        savedPage.getId(), parentPageId, workspaceId, savedPage.getTitle());
            } else {
                // 루트 page면 workspace에 root page로 추가
                workspace.getRootPages().add(savedPage);
                workspaceRepository.save(workspace);
                // 📄 비즈니스 이벤트: 루트 페이지 생성
                log.info("루트 페이지 생성 완료: pageId={}, workspaceId={}, title={}", 
                        savedPage.getId(), workspaceId, savedPage.getTitle());
            }

            log.info("페이지 생성 완료: pageId={}, workspaceId={}", savedPage.getId(), workspaceId);
            return savedPage;

        } catch (WorkSpaceNotFoundException | PageNotFoundException | PageOperationException e) {
            // 의도된 비즈니스 예외는 그대로 전파
            throw e;
        } catch (Exception e) {
            // 예상치 못한 예외는 래핑해서 전파
            handleAndThrowPageException("createPage", e);
            return null; // 실제로는 도달하지 않음
        }
    }

    /**
     * 페이지를 삭제합니다
     * @param pageId 삭제할 페이지의 ID
     */
    @Transactional
    public void deletePage(UUID pageId) {
        try {
            log.info("페이지 삭제 시작: pageId={}", pageId);
            
            // 입력값 검증
            if (pageId == null) {
                throw new PageOperationException("페이지 ID는 필수입니다");
            }

            // PageEntity 존재 여부 확인 (존재하지 않으면 예외 발생)
            if (!pageRepository.existsById(pageId)) {
                throw new PageNotFoundException("페이지가 존재하지 않습니다: " + pageId);
            }

            // PageEntity 삭제
            pageRepository.deleteById(pageId);
            
            // 📄 비즈니스 이벤트: 페이지 삭제 완료
            log.warn("페이지 삭제 실행: pageId={} - 데이터 손실 가능성", pageId);
            log.info("페이지 삭제 완료: pageId={}", pageId);

        } catch (PageNotFoundException | PageOperationException e) {
            // 의도된 비즈니스 예외는 그대로 전파
            throw e;
        } catch (Exception e) {
            // 예상치 못한 예외는 래핑해서 전파
            handleAndThrowPageException("deletePage", e);
        }
    }

    /**
     * 페이지 정보를 조회합니다
     * @param pageId 조회할 페이지의 ID
     * @return 조회된 페이지의 DTO
     */
    public PageDTO getPageDTO(UUID pageId)
    {
        try{

            PageEntity page = getPageEntityOrThrow(pageId);

            // PageEntity를 PageDTO로 변환
            PageDTO pageDTO = PageConverter.toDTO(page);

            return pageDTO;

        } catch(Exception e)
        {
            handleAndThrowPageException("getPage", e);
            return null; // 실제로는 도달하지 않음
        }
    }


    /**
     * Page Id를 통해 PageEntity 가져오기 시도 메서드
     * @param pageId 조회할 페이지 ID
     * @return PageEntity 검색 결과  
     * @throws PageNotFoundException 페이지가 존재하지 않을 경우
     */
    private PageEntity getPageEntityOrThrow(UUID pageId) {
        return pageRepository.findById(pageId)
                .orElseThrow(() -> {
                    log.warn("페이지 조회 실패: ID가 존재하지 않음, pageId={}", pageId);
                    return new PageNotFoundException("페이지가 존재하지 않습니다: " + pageId);
                });
    }

    /**
     * Page Id를 통해 Page의 Block Id만 가져오는 메서드
     * @param pageId 검색할 Page의 Id
     * @return 해당 Page의 Block Id
     * @throws PageNotFoundException 페이지가 존재하지 않을 경우
     */
    public UUID getBlockIdByPageId(UUID pageId)
    {
        try{
            // PageEntity 가져오기
            PageEntity page = getPageEntityOrThrow(pageId);

            // Block Id 반환
            UUID blockId =  page.getBlockId();
            return blockId;


        } catch(Exception e)
        {
            handleAndThrowPageException("getBlockIdByPageId", e);
            return null;
        }
    }

    /**
     * 페이지 제목을 수정합니다
     * @param pageId 수정할 페이지의 ID
     * @param newName 새로운 페이지 제목
     */
    @Transactional
    public void editPageName(UUID pageId, String newName)
    {
        try{
            // PageEntity 가져오기
            PageEntity page = getPageEntityOrThrow(pageId);

            // TODO : Page Title 제약 검증(이후 필요 시 추가)

            // 페이지 제목 수정
            page.setTitle(newName);

            // 페이지 저장
            pageRepository.save(page);


        } catch(Exception e)
        {   

            handleAndThrowPageException("editPageName", e);
        }
    } 

    /**
     * 양방향 관계 설정을 위해 부모 페이지에 자식 페이지 정보를 추가합니다
     * @param parentPage 부모 페이지 엔티티
     * @param childPage 자식 페이지 엔티티
     */
    public void addChildPage(PageEntity parentPage, PageEntity childPage)
    {          
        try{
            //parent page의 childPages에 새 page 추가 후 저장
            parentPage.getChildPages().add(childPage);
            pageRepository.save(parentPage);

        } catch(Exception e)
        {
            handleAndThrowPageException("addChildPage", e);
        }
 
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
