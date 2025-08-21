package com.example.TacoHub.Service;

import com.example.TacoHub.Exception.RedisOperationException;
import com.example.TacoHub.Exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Redis 작업을 위한 서비스 클래스
 * 
 * @param <T> Redis에 저장될 값의 타입
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService<T> extends BaseService {

    @Autowired
    private final RedisTemplate<String, T> redisTemplate;

    // ========== 공통 검증 메서드 ==========
    
    /**
     * Redis 키 유효성 검증
     * @param key 검증할 키
     * @param paramName 매개변수명 (로그용)
     */
    private void validateKey(String key, String paramName) {
        if (isStringNullOrEmpty(key)) {
            throw new RedisOperationException(paramName + "은(는) 필수 입력 항목입니다. Redis 키를 입력해주세요.");
        }
        if (isStringTooLong(key, 512)) {
            throw new RedisOperationException(paramName + "은(는) 512자를 초과할 수 없습니다. 현재 길이: " + key.length() + "자");
        }
        // Redis 키에 사용할 수 없는 문자 검증
        if (key.contains(" ") || key.contains("\n") || key.contains("\r") || key.contains("\t")) {
            throw new RedisOperationException(paramName + "에는 공백, 개행문자, 탭 문자를 포함할 수 없습니다.");
        }
    }

    /**
     * Redis 값 유효성 검증
     * @param value 검증할 값
     * @param paramName 매개변수명 (로그용)
     */
    private void validateValue(T value, String paramName) {
        if (isNull(value)) {
            throw new RedisOperationException(paramName + "은(는) 필수 입력 항목입니다. 저장할 값을 입력해주세요.");
        }
    }

    /**
     * Duration 유효성 검증
     * @param duration 검증할 지속 시간
     * @param paramName 매개변수명 (로그용)
     */
    private void validateDuration(Duration duration, String paramName) {
        if (isNull(duration)) {
            throw new RedisOperationException(paramName + "은(는) 필수 입력 항목입니다. 유효 시간을 지정해주세요.");
        }
        if (duration.isNegative()) {
            throw new RedisOperationException(paramName + "은(는) 음수일 수 없습니다. 양수 값을 입력해주세요.");
        }
        if (duration.isZero()) {
            throw new RedisOperationException(paramName + "은(는) 0일 수 없습니다. 유효한 시간을 입력해주세요.");
        }
        // 너무 긴 지속 시간 검증 (예: 1년 이상)
        if (duration.toDays() > 365) {
            throw new RedisOperationException(paramName + "이(가) 너무 깁니다. 1년 이하로 설정해주세요. 현재: " + duration.toDays() + "일");
        }
    }

    /**
     * Redis에 키-값 쌍을 저장하는 메서드
     * 
     * @param key Redis에 저장할 키
     * @param value Redis에 저장할 값
     * @param duration 값이 Redis에 저장될 기간
     * @throws RedisOperationException Redis 작업 중 오류 발생 시
     */
    public void setValues(String key, T value, Duration duration) {
        String methodName = "setValues";
        log.debug("[{}] Redis 값 저장 시작: key={}", methodName, key);
        
        try {
            // 1. 입력값 검증
            validateKey(key, "Redis 키");
            validateValue(value, "Redis 값");
            validateDuration(duration, "지속 시간");

            // 2. Redis에 값 저장
            redisTemplate.opsForValue().set(key, value, duration);
            
            log.debug("[{}] Redis 값 저장 완료: key={}", methodName, key);
            
        } catch (RedisOperationException e) {
            log.warn("[{}] 비즈니스 예외 발생: {}", methodName, e.getMessage());
            throw e;
        } catch (BusinessException e) {
            log.warn("[{}] 비즈니스 계층 예외 발생: type={}, message={}", 
                    methodName, e.getClass().getSimpleName(), e.getMessage());
            throw e;
        } catch (Exception e) {
            handleAndThrowRedisException(methodName, e);
        }
    }

    /**
     * Redis에서 키에 해당하는 값을 조회하는 메서드
     * 
     * @param key 조회할 값의 키
     * @return 키에 해당하는 값, 존재하지 않을 경우 null 반환
     * @throws RedisOperationException Redis 작업 중 오류 발생 시
     */
    public T getValues(String key) {
        String methodName = "getValues";
        log.debug("[{}] Redis 값 조회 시작: key={}", methodName, key);
        
        try {
            // 1. 입력값 검증
            validateKey(key, "Redis 키");

            // 2. Redis에서 값 조회
            T value = redisTemplate.opsForValue().get(key);
            
            log.debug("[{}] Redis 값 조회 완료: key={}, exists={}", methodName, key, value != null);
            return value;
            
        } catch (RedisOperationException e) {
            log.warn("[{}] 비즈니스 예외 발생: {}", methodName, e.getMessage());
            throw e;
        } catch (BusinessException e) {
            log.warn("[{}] 비즈니스 계층 예외 발생: type={}, message={}", 
                    methodName, e.getClass().getSimpleName(), e.getMessage());
            throw e;
        } catch (Exception e) {
            handleAndThrowRedisException(methodName, e);
            return null; // 실제로는 도달하지 않음
        }
    }

    /**
     * Redis에 특정 키가 존재하는지 확인하는 메서드
     * 
     * @param key 존재 여부를 확인할 키
     * @return 키가 존재하면 true, 그렇지 않으면 false
     * @throws RedisOperationException Redis 작업 중 오류 발생 시
     */
    public boolean checkExistsValue(String key) {
        String methodName = "checkExistsValue";
        log.debug("[{}] Redis 키 존재 확인 시작: key={}", methodName, key);
        
        try {
            // 1. 입력값 검증
            validateKey(key, "Redis 키");

            // 2. Redis에서 키 존재 여부 확인
            Boolean exists = redisTemplate.hasKey(key);
            boolean result = exists != null && exists;
            
            log.debug("[{}] Redis 키 존재 확인 완료: key={}, exists={}", methodName, key, result);
            return result;
            
        } catch (RedisOperationException e) {
            log.warn("[{}] 비즈니스 예외 발생: {}", methodName, e.getMessage());
            throw e;
        } catch (BusinessException e) {
            log.warn("[{}] 비즈니스 계층 예외 발생: type={}, message={}", 
                    methodName, e.getClass().getSimpleName(), e.getMessage());
            throw e;
        } catch (Exception e) {
            handleAndThrowRedisException(methodName, e);
            return false; // 실제로는 도달하지 않음
        }
    }

    /**
     * Redis에서 키-값 쌍을 삭제하는 메서드
     * 
     * @param key 삭제할 키-값 쌍의 키
     * @throws RedisOperationException Redis 작업 중 오류 발생 시
     */
    public void deleteValues(String key) {
        String methodName = "deleteValues";
        log.debug("[{}] Redis 값 삭제 시작: key={}", methodName, key);
        
        try {
            // 1. 입력값 검증
            validateKey(key, "Redis 키");

            // 2. Redis에서 값 삭제
            redisTemplate.delete(key);
            
            log.debug("[{}] Redis 값 삭제 완료: key={}", methodName, key);
            
        } catch (RedisOperationException e) {
            log.warn("[{}] 비즈니스 예외 발생: {}", methodName, e.getMessage());
            throw e;
        } catch (BusinessException e) {
            log.warn("[{}] 비즈니스 계층 예외 발생: type={}, message={}", 
                    methodName, e.getClass().getSimpleName(), e.getMessage());
            throw e;
        } catch (Exception e) {
            handleAndThrowRedisException(methodName, e);
        }
    }

    /**
     * 공통 Redis 예외 처리 메서드
     * 예외 타입에 따라 자동으로 warn/error 로깅을 결정
     * 
     * @param methodName 실패한 메서드명
     * @param originalException 원본 예외
     * @throws RedisOperationException 래핑된 예외
     */
    private void handleAndThrowRedisException(String methodName, Exception originalException) {
        RedisOperationException customException = new RedisOperationException(
            String.format("%s 실패 [%s]: %s", methodName, 
                         originalException.getClass().getSimpleName(), 
                         originalException.getMessage()),
            originalException
        );
        
        // BaseService의 메서드를 사용하여 예외 타입에 따라 warn/error 로깅
        handleAndThrow(methodName, originalException, customException);
    }

}
