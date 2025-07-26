package com.example.TacoHub.Controller.NotionCopyController;

import com.example.TacoHub.Converter.NotionCopyConveter.WorkSpaceConverter;
import com.example.TacoHub.Dto.NotionCopyDTO.Request.CreateWorkspaceRequest;
import com.example.TacoHub.Dto.NotionCopyDTO.Request.InviteUserRequest;
import com.example.TacoHub.Dto.NotionCopyDTO.Request.UpdateUserRoleRequest;
import com.example.TacoHub.Dto.NotionCopyDTO.Request.RemoveUserRequest;
import com.example.TacoHub.Dto.NotionCopyDTO.Response.ApiResponse;
import com.example.TacoHub.Dto.NotionCopyDTO.WorkSpaceDTO;
import com.example.TacoHub.Dto.NotionCopyDTO.WorkSpaceUserDTO;
import com.example.TacoHub.Entity.NotionCopyEntity.WorkSpaceEntity;
import com.example.TacoHub.Enum.NotionCopyEnum.WorkSpaceRole;
import com.example.TacoHub.Service.NotionCopyService.WorkSpaceService;
import com.example.TacoHub.Service.NotionCopyService.WorkSpaceUserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.UUID;

/**
 * 워크스페이스 관리 REST API Controller
 * 워크스페이스 CRUD 및 연관 페이지 관리 기능 제공
 */
@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
@Slf4j
public class WorkspaceController {

    private final WorkSpaceService workspaceService;
    private final WorkSpaceUserService workSpaceUserService;

    /**
     * 워크스페이스 생성
     * @param request 워크스페이스 생성 요청
     * @return 생성된 워크스페이스 응답
     */
    @PostMapping
    public ResponseEntity<ApiResponse<WorkSpaceDTO>> createWorkspace(@Valid @RequestBody CreateWorkspaceRequest request) {
        log.info("워크스페이스 생성 요청: name={}", request.getName());

        // workspace 생성 
        WorkSpaceEntity createdEntity = workspaceService.createWorkspaceEntity(request.getName());
        WorkSpaceDTO createdDto = WorkSpaceConverter.toDTO(createdEntity);

        log.info("워크스페이스 생성 완료: name={}", request.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("워크스페이스가 성공적으로 생성되었습니다.", createdDto));
    }

    /**
     * 워크스페이스 상세 조회
     * @param workspaceId 워크스페이스 ID
     * @return 워크스페이스 응답
     */
    @GetMapping("/{workspaceId}")
    public ResponseEntity<ApiResponse<WorkSpaceDTO>> getWorkspace(@PathVariable UUID workspaceId) {
        log.info("워크스페이스 조회 요청: workspaceId={}", workspaceId);

        // workspace 조회
        WorkSpaceDTO workspaceDto = workspaceService.getWorkspaceDto(workspaceId);
        log.info("워크스페이스 조회 완료: workspaceId={}", workspaceId);

        return ResponseEntity.ok(ApiResponse.success("워크스페이스 조회가 완료되었습니다.", workspaceDto));
    }


    /**
     * 워크스페이스 삭제
     * @param workspaceId 워크스페이스 ID
     * @return 삭제 완료 응답
     */
    @DeleteMapping("/{workspaceId}")
    public ResponseEntity<ApiResponse<Void>> deleteWorkspace(@PathVariable UUID workspaceId) {
        log.info("워크스페이스 삭제 요청: workspaceId={}", workspaceId);
        
        workspaceService.deleteWorkspace(workspaceId);
        
        log.info("워크스페이스 삭제 완료: workspaceId={}", workspaceId);
        return ResponseEntity.ok(ApiResponse.success("워크스페이스가 성공적으로 삭제되었습니다.", null));
    }


    /**
     * 워크스페이스에 사용자 초대
     * @param workspaceId 워크스페이스 ID
     * @param request 사용자 초대 요청
     * @return 초대 결과 응답
     */
    @PostMapping("/{workspaceId}/invite")
    public ResponseEntity<ApiResponse<String>> inviteUser(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody InviteUserRequest request) {
        log.info("사용자 초대 요청: workspaceId={}, email={}, role={}", 
                workspaceId, request.getEmail(), request.getRole());

        // TODO: 초대 로직 구현 필요 (EmailService, AuthCodeService와 함께)
        // 현재는 기본적인 응답만 반환
        
        log.info("사용자 초대 완료: workspaceId={}, email={}", workspaceId, request.getEmail());
        return ResponseEntity.ok(ApiResponse.success(
                "초대 이메일이 발송되었습니다.", 
                "초대가 처리 중입니다."));
    }

    /**
     * 워크스페이스 내 사용자 역할 업데이트
     * @param workspaceId 워크스페이스 ID
     * @param request 사용자 역할 업데이트 요청
     * @return 업데이트 결과 응답
     */
    @PutMapping("/{workspaceId}/users/role")
    public ResponseEntity<ApiResponse<String>> updateUserRole(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody UpdateUserRoleRequest request) {
        log.info("사용자 역할 업데이트 요청: workspaceId={}, userId={}, role={}", 
                workspaceId, request.getUserId(), request.getRole());

        // WorkSpaceUserService의 updateUserRole 메서드 사용
        workSpaceUserService.updateUserRole(request.getUserId(), workspaceId, WorkSpaceRole.fromString(request.getRole()));


        log.info("사용자 역할 업데이트 완료: workspaceId={}, userId={}", workspaceId, request.getUserId());
        return ResponseEntity.ok(ApiResponse.success(
                "사용자 역할이 성공적으로 업데이트되었습니다.", 
                "역할 변경이 완료되었습니다."));
    }

    /**
     * 워크스페이스에서 사용자 제거
     * @param workspaceId 워크스페이스 ID
     * @param request 사용자 제거 요청
     * @return 제거 결과 응답
     */
    @DeleteMapping("/{workspaceId}/users")
    public ResponseEntity<ApiResponse<String>> removeUser(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody RemoveUserRequest request) {
        log.info("사용자 제거 요청: workspaceId={}, userId={}", workspaceId, request.getUserId());

        // WorkSpaceUserService의 deleteWorkSpaceUserEntites 메서드 사용
        workSpaceUserService.deleteWorkSpaceUserEntites(workspaceId, request.getUserId());
        
        log.info("사용자 제거 완료: workspaceId={}, userId={}", workspaceId, request.getUserId());
        return ResponseEntity.ok(ApiResponse.success(
                "사용자가 성공적으로 제거되었습니다.", 
                "사용자 제거가 완료되었습니다."));
    }

    /**
     * 워크스페이스 멤버 목록 조회
     * @param workspaceId 워크스페이스 ID
     * @return 멤버 목록 응답
     */
    @GetMapping("/{workspaceId}/members")
    public ResponseEntity<ApiResponse<String>> getWorkspaceMembers(@PathVariable UUID workspaceId) {
        log.info("워크스페이스 멤버 조회 요청: workspaceId={}", workspaceId);

        // TODO: 멤버 목록 조회 로직 구현 필요
        // WorkSpaceUserService에서 워크스페이스의 모든 멤버 조회
        
        log.info("워크스페이스 멤버 조회 완료: workspaceId={}", workspaceId);
        return ResponseEntity.ok(ApiResponse.success(
                "멤버 목록 조회가 완료되었습니다.", 
                "멤버 목록입니다."));
    }

    // ===== 초대 관련 API =====

    /**
     * 초대 수락
     * @param workspaceId 워크스페이스 ID
     * @param invitationToken 초대 토큰
     * @return 초대 수락 결과 응답
     */
    @PostMapping("/{workspaceId}/invitations/{invitationToken}/accept")
    public ResponseEntity<ApiResponse<String>> acceptInvitation(
            @PathVariable UUID workspaceId,
            @PathVariable String invitationToken) {
        log.info("초대 수락 요청: workspaceId={}, token={}", workspaceId, invitationToken);

        // TODO: 초대 수락 로직 구현 필요
        // AuthCodeService에서 토큰 검증 후 WorkSpaceUserService에서 사용자 활성화
        
        log.info("초대 수락 완료: workspaceId={}", workspaceId);
        return ResponseEntity.ok(ApiResponse.success(
                "초대가 성공적으로 수락되었습니다.", 
                "워크스페이스에 참여하였습니다."));
    }

    /**
     * 초대 거절
     * @param workspaceId 워크스페이스 ID
     * @param invitationToken 초대 토큰
     * @return 초대 거절 결과 응답
     */
    @PostMapping("/{workspaceId}/invitations/{invitationToken}/decline")
    public ResponseEntity<ApiResponse<String>> declineInvitation(
            @PathVariable UUID workspaceId,
            @PathVariable String invitationToken) {
        log.info("초대 거절 요청: workspaceId={}, token={}", workspaceId, invitationToken);

        // TODO: 초대 거절 로직 구현 필요
        // AuthCodeService에서 토큰 무효화
        
        log.info("초대 거절 완료: workspaceId={}", workspaceId);
        return ResponseEntity.ok(ApiResponse.success(
                "초대가 거절되었습니다.", 
                "초대를 거절하였습니다."));
    }

    /**
     * 초대 재발송
     * @param workspaceId 워크스페이스 ID
     * @param userId 사용자 ID
     * @return 초대 재발송 결과 응답
     */
    @PostMapping("/{workspaceId}/invitations/{userId}/resend")
    public ResponseEntity<ApiResponse<String>> resendInvitation(
            @PathVariable UUID workspaceId,
            @PathVariable Long userId) {
        log.info("초대 재발송 요청: workspaceId={}, userId={}", workspaceId, userId);

        // TODO: 초대 재발송 로직 구현 필요
        // EmailService에서 새로운 초대 이메일 발송
        
        log.info("초대 재발송 완료: workspaceId={}, userId={}", workspaceId, userId);
        return ResponseEntity.ok(ApiResponse.success(
                "초대가 재발송되었습니다.", 
                "초대 이메일을 다시 발송하였습니다."));
    }

    /**
     * 초대 취소
     * @param workspaceId 워크스페이스 ID
     * @param userId 사용자 ID
     * @return 초대 취소 결과 응답
     */
    @DeleteMapping("/{workspaceId}/invitations/{userId}")
    public ResponseEntity<ApiResponse<String>> cancelInvitation(
            @PathVariable UUID workspaceId,
            @PathVariable Long userId) {
        log.info("초대 취소 요청: workspaceId={}, userId={}", workspaceId, userId);

        // TODO: 초대 취소 로직 구현 필요
        // WorkSpaceUserService에서 INVITED 상태의 사용자 제거
        
        log.info("초대 취소 완료: workspaceId={}, userId={}", workspaceId, userId);
        return ResponseEntity.ok(ApiResponse.success(
                "초대가 취소되었습니다.", 
                "초대를 취소하였습니다."));
    }

    /**
     * 대기 중인 초대 목록 조회
     * @param workspaceId 워크스페이스 ID
     * @return 대기 중인 초대 목록 응답
     */
    @GetMapping("/{workspaceId}/invitations/pending")
    public ResponseEntity<ApiResponse<String>> getPendingInvitations(@PathVariable UUID workspaceId) {
        log.info("대기 중인 초대 조회 요청: workspaceId={}", workspaceId);

        // TODO: 대기 중인 초대 목록 조회 로직 구현 필요
        // WorkSpaceUserService에서 INVITED 상태의 사용자들 조회
        
        log.info("대기 중인 초대 조회 완료: workspaceId={}", workspaceId);
        return ResponseEntity.ok(ApiResponse.success(
                "대기 중인 초대 목록 조회가 완료되었습니다.", 
                "대기 중인 초대 목록입니다."));
    }



    /**
         * 워크스페이스 내 사용자 역할 조회
         * @param workspaceId 워크스페이스 ID
         * @param userId 사용자 ID
         * @return 역할 문자열
         */
        @GetMapping("/{workspaceId}/users/{userId}/role")
        public ResponseEntity<ApiResponse<WorkSpaceUserDTO>> getUserRoleInWorkspace(
                @PathVariable UUID workspaceId,
                @PathVariable String userId) {
                log.info("워크스페이스 내 사용자 역할 조회 요청: workspaceId={}, userId={}", workspaceId, userId);

                // WorkSpaceUserService에서 역할 조회
                WorkSpaceUserDTO role = workSpaceUserService.getWorkSpaceUserDTO(userId, workspaceId);
                if (role == null) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("해당 사용자의 역할을 찾을 수 없습니다."));
                }
                return ResponseEntity.ok(ApiResponse.success("사용자 역할 조회 성공", role));
        }

    // ===== 사용자 워크스페이스 관리 API =====

    /**
     * 사용자가 속한 워크스페이스 목록 조회
     * @return 사용자 워크스페이스 목록 응답
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<String>> getMyWorkspaces() {
        log.info("사용자 워크스페이스 목록 조회 요청");

        // TODO: 사용자 워크스페이스 목록 조회 로직 구현 필요
        // UserInfoExtractor에서 현재 사용자 이메일 추출 후 WorkSpaceUserService에서 워크스페이스 목록 조회
        
        log.info("사용자 워크스페이스 목록 조회 완료");
        return ResponseEntity.ok(ApiResponse.success(
                "워크스페이스 목록 조회가 완료되었습니다.", 
                "사용자 워크스페이스 목록입니다."));
    }



    // ===== 워크스페이스 설정 API =====

    /**
     * 워크스페이스 이름 수정
     * @param workspaceId 워크스페이스 ID
     * @param newName 새로운 워크스페이스 이름
     * @return 이름 수정 결과 응답
     */
    @PutMapping("/{workspaceId}/name")
    public ResponseEntity<ApiResponse<String>> updateWorkspaceName(
            @PathVariable UUID workspaceId,
            @RequestParam String newName) {
        log.info("워크스페이스 이름 수정 요청: workspaceId={}, newName={}", workspaceId, newName);

        // TODO: 이름 수정 로직 구현 필요
        // WorkSpaceService의 editWorkspaceName 메서드 사용
        
        log.info("워크스페이스 이름 수정 완료: workspaceId={}", workspaceId);
        return ResponseEntity.ok(ApiResponse.success(
                "워크스페이스 이름이 수정되었습니다.", 
                "이름이 성공적으로 변경되었습니다."));
    }


    /**
     * 워크스페이스 나가기 (자신을 워크스페이스에서 제거)
     * @param workspaceId 워크스페이스 ID
     * @return 워크스페이스 나가기 결과 응답
     */
    @PostMapping("/{workspaceId}/leave")
    public ResponseEntity<ApiResponse<String>> leaveWorkspace(@PathVariable UUID workspaceId) {
        log.info("워크스페이스 나가기 요청: workspaceId={}", workspaceId);

        // TODO: 워크스페이스 나가기 로직 구현 필요
        // UserInfoExtractor에서 현재 사용자 확인 후 WorkSpaceUserService에서 관계 삭제
        // OWNER인 경우 다른 ADMIN에게 소유권 이전 또는 워크스페이스 삭제 확인 필요
        
        log.info("워크스페이스 나가기 완료: workspaceId={}", workspaceId);
        return ResponseEntity.ok(ApiResponse.success(
                "워크스페이스에서 나갔습니다.", 
                "성공적으로 워크스페이스를 나갔습니다."));
    }










}
