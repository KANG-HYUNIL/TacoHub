package com.example.TacoHub.Service;

import com.example.TacoHub.Exception.RedisOperationException;
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
public class RedisService<T> {

    @Autowired
    private final RedisTemplate<String, T> redisTemplate;

    /**
     * Redis에 키-값 쌍을 저장하는 메서드
     * 
     * @param key Redis에 저장할 키
     * @param value Redis에 저장할 값
     * @param duration 값이 Redis에 저장될 기간
     * @throws RedisOperationException Redis 작업 중 오류 발생 시
     */
    public void setValues(String key, T value, Duration duration)
    {
        try {
            // 입력값 검증
            if (key == null || key.trim().isEmpty()) {
                throw new RedisOperationException("Redis 키는 필수입니다");
            }
            if (value == null) {
                throw new RedisOperationException("Redis 값은 필수입니다");
            }
            if (duration == null || duration.isNegative() || duration.isZero()) {
                throw new RedisOperationException("유효한 지속 시간이 필요합니다");
            }

            redisTemplate.opsForValue().set(key, value, duration);
        } catch (RedisOperationException e) {
            throw e; // 비즈니스 예외는 그대로 전파
        } catch (Exception e) {
            handleAndThrowRedisException("setValues", e);
        }
    }

    /**
     * Redis에서 키에 해당하는 값을 조회하는 메서드
     * 
     * @param key 조회할 값의 키
     * @return 키에 해당하는 값, 존재하지 않을 경우 null 반환
     * @throws RedisOperationException Redis 작업 중 오류 발생 시
     */
    public T getValues(String key)
    {
        try {
            // 입력값 검증
            if (key == null || key.trim().isEmpty()) {
                throw new RedisOperationException("Redis 키는 필수입니다");
            }

            return redisTemplate.opsForValue().get(key);
        } catch (RedisOperationException e) {
            throw e; // 비즈니스 예외는 그대로 전파
        } catch (Exception e) {
            handleAndThrowRedisException("getValues", e);
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
    public boolean checkExistsValue(String key)
    {
        try {
            // 입력값 검증
            if (key == null || key.trim().isEmpty()) {
                throw new RedisOperationException("Redis 키는 필수입니다");
            }

            Boolean exists = redisTemplate.hasKey(key);
            return exists != null && exists;
        } catch (RedisOperationException e) {
            throw e; // 비즈니스 예외는 그대로 전파
        } catch (Exception e) {
            handleAndThrowRedisException("checkExistsValue", e);
            return false; // 실제로는 도달하지 않음
        }
    }

    /**
     * Redis에서 키-값 쌍을 삭제하는 메서드
     * 
     * @param key 삭제할 키-값 쌍의 키
     * @throws RedisOperationException Redis 작업 중 오류 발생 시
     */
    public void deleteValues(String key)
    {
        try {
            // 입력값 검증
            if (key == null || key.trim().isEmpty()) {
                throw new RedisOperationException("Redis 키는 필수입니다");
            }

            redisTemplate.delete(key);
        } catch (RedisOperationException e) {
            throw e; // 비즈니스 예외는 그대로 전파
        } catch (Exception e) {
            handleAndThrowRedisException("deleteValues", e);
        }
    }

    /**
     * 공통 Redis 예외 처리 메서드
     * @param methodName 실패한 메서드명
     * @param originalException 원본 예외
     * @throws RedisOperationException 래핑된 예외
     */
    private void handleAndThrowRedisException(String methodName, Exception originalException) {
        String errorMessage = originalException.getMessage();
        String exceptionType = originalException.getClass().getSimpleName();
        
        log.error("{} 실패: type={}, message={}", methodName, exceptionType, errorMessage, originalException);
        
        throw new RedisOperationException(
            String.format("%s 실패 [%s]: %s", methodName, exceptionType, errorMessage),
            originalException
        );
    }


}
