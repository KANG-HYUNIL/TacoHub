package com.example.TacoHub.Service.NotionCopyService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.TacoHub.Dto.NotionCopyDTO.Request.InviteUserRequest;
import com.example.TacoHub.Dto.NotionCopyDTO.Request.ResendInvitationRequest;
import com.example.TacoHub.Dto.NotionCopyDTO.Response.InvitationAcceptResponse;
import com.example.TacoHub.Dto.NotionCopyDTO.Response.InvitationResponse;
import com.example.TacoHub.Dto.NotionCopyDTO.Response.PendingInvitationResponse;
import com.example.TacoHub.Entity.NotionCopyEntity.InvitationEntity;
import com.example.TacoHub.Enum.NotionCopyEnum.InvitationStatus;
import com.example.TacoHub.Enum.NotionCopyEnum.WorkSpaceRole;
import com.example.TacoHub.Exception.NotionCopyException.InvitationOperationException;
import com.example.TacoHub.Exception.BusinessException;
import com.example.TacoHub.Logging.AuditLogging;
import com.example.TacoHub.Logging.UserInfoExtractor;
import com.example.TacoHub.Repository.NotionCopyRepository.InvitationRepository;
import com.example.TacoHub.Service.AccountService;
import com.example.TacoHub.Service.BaseService;
import com.example.TacoHub.Service.EmailService;
import com.example.TacoHub.Service.RedisService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 초대 관련 비즈니스 로직을 처리하는 서비스 클래스
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class InvitationService extends BaseService {
    
    private final InvitationRepository invitationRepository;
    private final RedisService<String> redisService;
    private final WorkSpaceUserService workSpaceUserService;
    private final WorkSpaceService workSpaceService;
    private final UserInfoExtractor userInfoExtractor;
    private final EmailService emailService;
    private final AccountService accountService;
    
    // Redis key 패턴
    private static final String INVITATION_REDIS_KEY = "invitation_token:";
    private static final String INVITATION_EMAIL_KEY = "invitation_email:";
    
    // 기본 만료 시간 (7일)
    private static final int DEFAULT_EXPIRATION_DAYS = 7;
    
    // ========== 초대 생성 관련 메서드 ==========
    
    /**
     * 워크스페이스에 사용자 초대
     * @param workspaceId 워크스페이스 ID
     * @param request 초대 요청 정보 (이메일, 역할, 메시지 등)
     * @return 초대 생성 결과
     */
    @AuditLogging(action = "사용자_초대", includeParameters = true, includePerformance = true)
    public InvitationResponse createInvitation(UUID workspaceId, InviteUserRequest request) {
        String methodName = "createInvitation";
        log.info("[{}] 워크스페이스 초대 시작: workspaceId={}, email={}, role={}", 
                methodName, workspaceId, request.getEmail(), request.getRole());
        
        try {
            // 1. 입력값 검증
            validateWorkspaceId(workspaceId, methodName);
            validateInviteUserRequest(request, methodName);
            
            // 2. 현재 사용자 정보 추출 및 권한 확인
            String currentUserEmail = userInfoExtractor.getCurrentUserEmail();
            if (isStringNullOrEmpty(currentUserEmail)) {
                log.warn("[{}] 권한 검증 실패: 현재 사용자 정보를 찾을 수 없음", methodName);
                throw new InvitationOperationException("현재 사용자 정보를 확인할 수 없습니다. 다시 로그인해주세요.");
            }
            
            // 3. 초대 권한 확인
            if (!workSpaceUserService.canUserInviteAndDeleteUsers(currentUserEmail, workspaceId)) {
                log.warn("[{}] 권한 검증 실패: 초대 권한 없음, currentUser={}, workspaceId={}", 
                        methodName, currentUserEmail, workspaceId);
                throw new InvitationOperationException("워크스페이스에서 사용자를 초대할 권한이 없습니다.");
            }
            
            // 4. 실제 초대 처리
            WorkSpaceRole role = request.getRole();
            return inviteUser(workspaceId, request.getEmail(), role, request.getMessage(), request.getExpirationDays());
            
        } catch (InvitationOperationException e) {
            // 1단계: 해당 메서드 자체 throw catch (구체적 예외)
            log.warn("[{}] 초대 생성 입력값 오류: workspaceId={}, email={}, 원인={}", 
                    methodName, workspaceId, request.getEmail(), e.getMessage());
            throw e;
        } catch (BusinessException e) {
            // 2단계: 상위 전파 의식한 비즈니스 catch (모든 비즈니스 예외)
            log.warn("[{}] 초대 생성 비즈니스 오류: workspaceId={}, email={}, 원인={}", 
                    methodName, workspaceId, request.getEmail(), e.getMessage());
            throw e;
        } catch (Exception e) {
            // 3단계: 시스템 예외 catch
            handleAndThrowInvitationException(methodName, e);
            return null; // 실제로는 도달하지 않음
        }
    }
    
    /**
     * 특정 역할로 사용자 초대 (내부 메서드)
     * @param workspaceId 워크스페이스 ID
     * @param email 초대할 이메일
     * @param role 부여할 역할
     * @param customMessage 초대 메시지
     * @param expirationDays 만료 일수
     * @return 초대 생성 결과
     */
    @AuditLogging(action = "사용자_초대_처리", includeParameters = true, includePerformance = true)
    @Transactional
    public InvitationResponse inviteUser(UUID workspaceId, String email, WorkSpaceRole role, 
                                       String customMessage, Integer expirationDays) {
        String methodName = "inviteUser";
        log.info("[{}] 사용자 초대 처리 시작: workspaceId={}, email={}, role={}", 
                methodName, workspaceId, email, role);
        
        try {
            // 1. 입력값 검증
            validateWorkspaceId(workspaceId, methodName);
            validateEmail(email, methodName);
            validateRole(role, methodName);
            
            // 2. 계정 존재 여부 확인 (초대는 기존 계정 사용자에게만 가능)
            if (!accountService.existsByEmail(email)) {
                log.warn("[{}] 존재하지 않는 계정 초대 시도: email={}, workspaceId={}", methodName, email, workspaceId);
                throw new InvitationOperationException("해당 이메일로 가입된 계정이 존재하지 않습니다. 먼저 회원가입을 진행해주세요.");
            }


            // 3. 중복 초대 확인
            if (isDuplicateInvitation(email, workspaceId)) {
                
                InvitationEntity invitationEntity = invitationRepository.
                    findByInvitedEmailAndWorkspaceIdAndStatus(email, workspaceId, InvitationStatus.PENDING).get();

                    // 만료된 초대인지 확인
                if (isInvitationExpired(invitationEntity)) {
                    log.warn("[{}] 만료된 초대: token={}, expiresAt={}",
                            methodName, invitationEntity.getInvitationToken(), invitationEntity.getExpiresAt());

                    // 만료된 초대는 상태를 EXPIRED로 변경
                    invitationEntity.setStatus(InvitationStatus.EXPIRED);
                    invitationRepository.save(invitationEntity);
                    
                    throw new InvitationOperationException("만료된 초대입니다. 새로운 초대를 요청해주세요.");

                }
                
                log.warn("[{}] 중복 초대 시도: email={}, workspaceId={}", methodName, email, workspaceId);
                throw new InvitationOperationException("이미 대기 중인 초대가 존재합니다.");
            }
            
            // 4. 이미 워크스페이스 멤버인지 확인 (권한 체크를 통해 간접 확인)
            if (workSpaceUserService.canUserViewPage(email, workspaceId)) {
                log.warn("[{}] 기존 멤버 초대 시도: email={}, workspaceId={}", methodName, email, workspaceId);
                throw new InvitationOperationException("이미 워크스페이스의 멤버입니다.");
            }
            
            // 5. 워크스페이스 정보 조회
            var workspace = workSpaceService.getWorkSpaceEntityOrThrow(workspaceId);
            String currentUserEmail = userInfoExtractor.getCurrentUserEmail();
            
            // 5. 초대 토큰 생성 및 만료 시간 계산
            String invitationToken = generateInvitationToken();
            LocalDateTime expiresAt = calculateExpirationTime(expirationDays);
            
            // 6. 초대 엔티티 생성 및 저장
            InvitationEntity invitation = InvitationEntity.builder()
                    .invitationToken(UUID.fromString(invitationToken))
                    .invitedBy(currentUserEmail)
                    .invitedEmail(email)
                    .workspaceId(workspaceId)
                    .role(role)
                    .status(InvitationStatus.PENDING)
                    .expiresAt(expiresAt)
                    .customMessage(customMessage)
                    .build();
            
            invitationRepository.save(invitation);
            
            
            // 8. 이메일 전송
            boolean emailSent = emailService.sendInvitationEmail(
                    email, invitationToken, workspace.getName(), 
                    currentUserEmail, role.name(), customMessage);
            
            // 9. 응답 생성
            InvitationResponse response = InvitationResponse.builder()
                    .invitationToken(invitationToken)
                    .invitedEmail(email)
                    .role(role.name())
                    .invitedAt(LocalDateTime.now())
                    .expiresAt(expiresAt)
                    .invitedBy(currentUserEmail)
                    .message(customMessage)
                    .isEmailSent(emailSent)
                    .build();
            
            log.info("[{}] 사용자 초대 처리 완료: workspaceId={}, email={}, token={}, emailSent={}", 
                    methodName, workspaceId, email, invitationToken, emailSent);
            
            return response;
            
        } catch (InvitationOperationException e) {
            // 1단계: 해당 메서드 자체 throw catch (구체적 예외)
            log.warn("[{}] 사용자 초대 입력값 오류: workspaceId={}, email={}, 원인={}", 
                    methodName, workspaceId, email, e.getMessage());
            throw e;
        } catch (BusinessException e) {
            // 2단계: 상위 전파 의식한 비즈니스 catch (모든 비즈니스 예외)
            log.warn("[{}] 사용자 초대 비즈니스 오류: workspaceId={}, email={}, 원인={}", 
                    methodName, workspaceId, email, e.getMessage());
            throw e;
        } catch (Exception e) {
            // 3단계: 시스템 예외 catch
            handleAndThrowInvitationException(methodName, e);
            return null; // 실제로는 도달하지 않음
        }
    }
    
    // ========== 초대 수락 관련 메서드 ==========

    
/**
 * 초대 토큰으로 초대 수락 처리
 * @param token 초대 토큰
 * @return 초대 수락 결과
 */
@AuditLogging(action = "초대_수락", includeParameters = true, includePerformance = true)
@Transactional
public InvitationAcceptResponse acceptInvitationByToken(String token) {
    String methodName = "acceptInvitationByToken";
    log.info("[{}] 초대 수락 처리 시작: token={}", methodName, token);
    
    try {
        // 1. 입력값 검증
        if (isStringNullOrEmpty(token)) {
            log.warn("[{}] 토큰 검증 실패: 토큰이 null 또는 빈 문자열", methodName);
            throw new InvitationOperationException("초대 토큰은 필수입니다.");
        }
        

        

        // 3. DB에서 초대 엔티티 조회 (정확한 상태 확인을 위한 필수 과정)
        InvitationEntity invitation = invitationRepository.findByInvitationToken(UUID.fromString(token))
            .orElseThrow(() -> {
                log.warn("[{}] 초대 토큰을 찾을 수 없음: token={}", methodName, token);
                return new InvitationOperationException("유효하지 않은 초대 토큰입니다.");
            });
        
        // 4. 초대 상태 검증
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            log.warn("[{}] 잘못된 초대 상태: token={}, status={}", 
                    methodName, token, invitation.getStatus());
            throw new InvitationOperationException("이미 처리된 초대이거나 취소된 초대입니다.");
        }
        
        // 5. 초대 만료 시간 검증
        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("[{}] 만료된 초대: token={}, expiresAt={}", 
                    methodName, token, invitation.getExpiresAt());
            
            // 만료된 초대는 상태를 EXPIRED로 변경
            invitation.setStatus(InvitationStatus.EXPIRED);
            invitationRepository.save(invitation);
            
            throw new InvitationOperationException("만료된 초대입니다. 새로운 초대를 요청해주세요.");
        }
        
        // 6. 이미 워크스페이스 멤버인지 재확인
        if (workSpaceUserService.canUserViewPage(invitation.getInvitedEmail(), invitation.getWorkspaceId())) {
            log.warn("[{}] 이미 워크스페이스 멤버: email={}, workspaceId={}", 
                    methodName, invitation.getInvitedEmail(), invitation.getWorkspaceId());
            
            // 이미 멤버인 경우 초대 상태를 ACCEPTED로 변경 (중복 처리 방지)
            invitation.setStatus(InvitationStatus.ACCEPTED);
            invitation.setAcceptedAt(LocalDateTime.now());
            invitationRepository.save(invitation);
            
            throw new InvitationOperationException("이미 워크스페이스의 멤버입니다.");
        }
        

        
        // 8. 워크스페이스에 사용자 추가 (실제 멤버십 생성)

        switch (invitation.getRole())
        {
            case ADMIN -> {
                workSpaceUserService.inviteAsAdmin(invitation.getInvitedEmail(), invitation.getWorkspaceId());
            }

            case MEMBER -> {
                workSpaceUserService.inviteAsMember(invitation.getInvitedEmail(), invitation.getWorkspaceId());
            }

            case GUEST -> {
                workSpaceUserService.inviteAsGuest(invitation.getInvitedEmail(), invitation.getWorkspaceId());
            }
        }


        
        log.info("[{}] 워크스페이스 멤버 추가 완료: email={}, workspaceId={}, role={}", 
                methodName, invitation.getInvitedEmail(), invitation.getWorkspaceId(), invitation.getRole());
        
        // 9. 초대 상태를 ACCEPTED로 변경
        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitation.setAcceptedAt(LocalDateTime.now());
        invitationRepository.save(invitation);
        

        
        // 11. 성공 응답 생성
        InvitationAcceptResponse response = InvitationAcceptResponse.builder()
                .workspaceId(invitation.getWorkspaceId().toString())
                .workspaceName(workSpaceService.getWorkSpaceEntityOrThrow(invitation.getWorkspaceId()).getName())
                .userEmail(invitation.getInvitedEmail())
                .role(invitation.getRole().name())
                .acceptedAt(invitation.getAcceptedAt())
                .invitedBy(invitation.getInvitedBy())
                .message("워크스페이스에 성공적으로 참여했습니다.")
                .build();
        
        log.info("[{}] 초대 수락 처리 완료: token={}, email={}, workspaceId={}", 
                methodName, token, invitation.getInvitedEmail(), invitation.getWorkspaceId());
        
        return response;
        
    } catch (InvitationOperationException e) {
        log.warn("[{}] 초대 수락 입력값 오류: token={}, 원인={}", methodName, token, e.getMessage());
        throw e;
    } catch (BusinessException e) {
        log.warn("[{}] 초대 수락 비즈니스 오류: token={}, 원인={}", methodName, token, e.getMessage());
        throw e;
    } catch (Exception e) {
        handleAndThrowInvitationException(methodName, e);
        return null; // 실제로는 도달하지 않음
    }
}
    
    // ========== 초대 조회 관련 메서드 ==========
    
    /**
     * 워크스페이스의 대기 중인 초대 목록 조회
     * @param workspaceId 워크스페이스 ID
     * @return 대기 중인 초대 목록
     */
    public List<InvitationEntity> getPendingInvitations(UUID workspaceId) {

        Optional<List<InvitationEntity>> pendingInvitations = invitationRepository.findByWorkspaceIdAndStatus(workspaceId, InvitationStatus.PENDING);

        return pendingInvitations.orElseGet(Collections::emptyList);
    }

    
    /**
     * 특정 초대 토큰 정보 조회
     * @param token 초대 토큰
     * @return 초대 정보 (토큰 검증용)
     */
    public PendingInvitationResponse getInvitationInfo(String token) {
        // TODO: 구현 예정
        return null;
    }
    
    // ========== 초대 관리 관련 메서드 ==========
    
    /**
     * 초대 재전송
     * @param token 초대 토큰
     * @param request 재전송 요청 정보
     * @return 재전송 결과
     */
    public InvitationResponse resendInvitation(String token, ResendInvitationRequest request) {
        // TODO: 구현 예정
        return null;
    }
    
    /**
     * 초대 취소
     * @param token 초대 토큰
     * @param currentUserEmail 취소를 요청하는 사용자 이메일
     * @return 취소 성공 여부
     */
    public boolean cancelInvitation(String token, String currentUserEmail) {
        // TODO: 구현 예정
        return false;
    }
    
    // ========== 초대 검증 관련 메서드 ==========
    
    /**
     * 초대 토큰 유효성 검증
     * @param token 초대 토큰
     * @return 유효성 검증 결과
     */
    public boolean validateInvitationToken(String token) {
        // TODO: 구현 예정
        return false;
    }
    
    /**
     * 초대 만료 여부 확인
     * @param token 초대 토큰
     * @return 만료 여부
     */
    public boolean isInvitationExpired(InvitationEntity invitationEntity) {

        if (invitationEntity == null || invitationEntity.getExpiresAt() == null) {
            return true;
        }

        return invitationEntity.getExpiresAt().isBefore(LocalDateTime.now());
    }
    
    /**
     * 중복 및 기간 초과 초대 확인
     * @param email 초대할 이메일
     * @param workspaceId 워크스페이스 ID
     * @return 중복 여부
     */
    public boolean isDuplicateOrExpiredInvitation(String email, UUID workspaceId) {
        return invitationRepository.existsByInvitedEmailAndWorkspaceIdAndStatus(
            email, workspaceId, InvitationStatus.PENDING);
    }

        /**
     * 중복 및 기간 초과 초대 확인
     * @param email 초대할 이메일
     * @param workspaceId 워크스페이스 ID
     * @return 중복 여부
     */
    public boolean isDuplicateInvitation(String email, UUID workspaceId)      
    {   
        return invitationRepository.existsByInvitedEmailAndWorkspaceIdAndStatus(
            email, workspaceId, InvitationStatus.PENDING);
    }
    
    // ========== 유틸리티 메서드 ==========
    
    /**
     * 초대 토큰 생성
     * @return 생성된 UUID 토큰
     */
    private String generateInvitationToken() {
        return UUID.randomUUID().toString();
    }
    
    /**
     * 초대 만료 시간 계산
     * @param expirationDays 만료 일수
     * @return 만료 시간
     */
    private LocalDateTime calculateExpirationTime(Integer expirationDays) {
        int days = (expirationDays != null && expirationDays > 0) ? expirationDays : DEFAULT_EXPIRATION_DAYS;
        return LocalDateTime.now().plusDays(days);
    }
    


    
    // ========== 입력값 검증 메서드 ==========
    
    /**
     * 초대 요청 정보 검증
     * @param request 초대 요청 정보
     * @param methodName 호출 메서드명
     */
    private void validateInviteUserRequest(InviteUserRequest request, String methodName) {
        if (isNull(request)) {
            log.warn("초대 요청 검증 실패: 메서드={}, 원인=요청 정보가 null", methodName);
            throw new InvitationOperationException("초대 요청 정보는 필수입니다.");
        }
        
        if (isStringNullOrEmpty(request.getEmail())) {
            log.warn("초대 요청 검증 실패: 메서드={}, 원인=이메일이 필수", methodName);
            throw new InvitationOperationException("초대할 이메일 주소는 필수입니다.");
        }
        
        if (isStringNullOrEmpty(request.getRole().toString())) {
            log.warn("초대 요청 검증 실패: 메서드={}, 원인=역할이 필수", methodName);
            throw new InvitationOperationException("초대할 사용자의 역할은 필수입니다.");
        }
        
        // 역할 유효성 검증
        try {
            WorkSpaceRole.valueOf(request.getRole().toString());
        } catch (IllegalArgumentException e) {
            log.warn("초대 요청 검증 실패: 메서드={}, 원인=유효하지 않은 역할, role={}", 
                    methodName, request.getRole());
            throw new InvitationOperationException("유효하지 않은 역할입니다: " + request.getRole());
        }
    }
    
    /**
     * 워크스페이스 ID 검증
     * @param workspaceId 워크스페이스 ID
     * @param methodName 호출 메서드명
     */
    private void validateWorkspaceId(UUID workspaceId, String methodName) {
        if (isNull(workspaceId)) {
            log.warn("워크스페이스 ID 검증 실패: 메서드={}, 원인=워크스페이스 ID가 null", methodName);
            throw new InvitationOperationException("워크스페이스 ID는 필수입니다.");
        }
    }
    
    /**
     * 이메일 유효성 검증
     * @param email 이메일
     * @param methodName 호출 메서드명
     */
    private void validateEmail(String email, String methodName) {
        if (isStringNullOrEmpty(email)) {
            log.warn("이메일 검증 실패: 메서드={}, 원인=이메일이 null 또는 빈 문자열", methodName);
            throw new InvitationOperationException("이메일 주소는 필수입니다.");
        }
        
        if (isStringTooLong(email, 255)) {
            log.warn("이메일 검증 실패: 메서드={}, 원인=이메일이 너무 김, length={}", 
                    methodName, email.length());
            throw new InvitationOperationException("이메일 주소는 255자를 초과할 수 없습니다.");
        }
        
        // 기본적인 이메일 형식 검증
        if (!email.contains("@")) {
            log.warn("이메일 검증 실패: 메서드={}, 원인=잘못된 이메일 형식, email={}", 
                    methodName, email);
            throw new InvitationOperationException("올바른 이메일 형식이 아닙니다.");
        }
    }
    
    /**
     * 역할 유효성 검증
     * @param role 역할
     * @param methodName 호출 메서드명
     */
    private void validateRole(WorkSpaceRole role, String methodName) {
        if (isNull(role)) {
            log.warn("역할 검증 실패: 메서드={}, 원인=역할이 null", methodName);
            throw new InvitationOperationException("사용자 역할은 필수입니다.");
        }
    }
    
    /**
     * 공통 Invitation 예외 처리 메서드
     * @param methodName 실패한 메서드명
     * @param originalException 원본 예외
     */
    private void handleAndThrowInvitationException(String methodName, Exception originalException) {
        InvitationOperationException customException = new InvitationOperationException(
            String.format("%s 실패 [%s]: %s", methodName, 
                         originalException.getClass().getSimpleName(), 
                         originalException.getMessage()),
            originalException
        );
        
        // BaseService의 메서드를 사용하여 예외 타입에 따라 warn/error 로깅
        handleAndThrow(methodName, originalException, customException);
    }
}
