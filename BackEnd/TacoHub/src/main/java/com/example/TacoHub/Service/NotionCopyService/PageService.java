package com.example.TacoHub.Service.NotionCopyService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

import com.example.TacoHub.Converter.NotionCopyConveter.PageConverter;
import com.example.TacoHub.Dto.NotionCopyDTO.PageDTO;
import com.example.TacoHub.Entity.NotionCopyEntity.PageEntity;
import com.example.TacoHub.Entity.NotionCopyEntity.WorkSpaceEntity;
import com.example.TacoHub.Exception.NotionCopyException.PageNotFoundException;
import com.example.TacoHub.Exception.NotionCopyException.WorkSpaceNotFoundException;
import com.example.TacoHub.Exception.NotionCopyException.WorkSpaceOperationException;
import com.example.TacoHub.Repository.NotionCopyRepository.PageRepository;
import com.example.TacoHub.Repository.NotionCopyRepository.WorkSpaceRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class PageService {

    private final PageRepository pageRepository;
    private final BlockService blockService;
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
     * @return 생성된 페이지의 DTO
     */
    public PageDTO createPage(UUID workspaceId, UUID parentPageId)
    {
        try{
            // 기반 workspace 존재 검증
            Optional<WorkSpaceEntity> workspace = workspaceRepository.findById(workspaceId);
            if (workspace.isEmpty())
            {
                log.warn("");
                throw new WorkSpaceNotFoundException("");
            }

            // 부모 page 있을 시에 존재 검증
            PageEntity parentPage = null;
            if (parentPageId != null)
            {
                Optional<PageEntity> parentPageOptional = pageRepository.findById(parentPageId);
                if (parentPageOptional.isEmpty())
                {
                    log.warn("Parent page with ID {} not found in workspace {}", parentPageId, workspaceId);
                    throw new PageNotFoundException("Parent page not found");
                }

                parentPage = parentPageOptional.get();
            }

            // page의 block 생성하고 id 얻어내는 과정 필요
            // fixme

            //새 PageEntity 생성
            PageEntity newPage = PageEntity.builder()
                .title(newPageName)
                .workspace(workspace.get())
                .parentPage(parentPage)
                .isRoot(parentPage == null)
                .orderIndex(0) //fixme, 우선 0으로 대충 설정
                .build();

            // page 저장
            PageEntity savedPage = pageRepository.save(newPage);

            // parent 가 있는 page면
            if (parentPage != null)
            {
                // parent page에 새 page 추가
                addChildPage(parentPage, savedPage);
            }

            //parent가 없는 root page 면
            if (parentPage == null)
            {
                // workspace에 root page로 추가
                workspace.get().getRootPages().add(savedPage);
                workspaceRepository.save(workspace.get());
            }

            // PageEntity를 PageDTO로 변환
            PageDTO pageDTO = PageConverter.toDTO(savedPage);

            log.info("");
            return pageDTO;
        } catch(Exception e)
        {
            handleAndThrowPageException("createPage", e);
            return null; // 실제로는 도달하지 않음
        }

 
        
    }

    /**
     * 페이지를 삭제합니다
     * @param pageId 삭제할 페이지의 ID
     */
    public void deletePage(UUID pageId)
    {
        try{

        } catch(Exception e)
        {
            handleAndThrowPageException("deletePage", e);
        }
    }

    /**
     * 페이지 정보를 조회합니다
     * @param pageId 조회할 페이지의 ID
     * @return 조회된 페이지의 DTO
     */
    public PageDTO getPage(UUID pageId)
    {
        try{
            // TODO: 구현 필요
            return null;
        } catch(Exception e)
        {
            handleAndThrowPageException("getPage", e);
            return null; // 실제로는 도달하지 않음
        }
    }

    /**
     * 페이지 제목을 수정합니다
     * @param pageId 수정할 페이지의 ID
     * @param newName 새로운 페이지 제목
     */
    public void editPageName(UUID pageId, String newName)
    {
        try{

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
     * @throws WorkSpaceOperationException 래핑된 예외
     */
    private void handleAndThrowPageException(String methodName, Exception originalException) {
        String errorMessage = originalException.getMessage();
        String exceptionType = originalException.getClass().getSimpleName();
        
        log.error("{} 실패: type={}, message={}", methodName, exceptionType, errorMessage, originalException);
        
        throw new WorkSpaceOperationException(
            String.format("%s 실패 [%s]: %s", methodName, exceptionType, errorMessage),
            originalException
        );
    }


}
