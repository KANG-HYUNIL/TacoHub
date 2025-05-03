package com.example.TacoHub.Service;

import lombok.RequiredArgsConstructor;
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
public class RedisService<T> {

    @Autowired
    private final RedisTemplate<String, T> redisTemplate;

    /**
     * Redis에 키-값 쌍을 저장하는 메서드
     * 
     * @param key Redis에 저장할 키
     * @param value Redis에 저장할 값
     * @param duration 값이 Redis에 저장될 기간
     */
    public void setValues(String key, T value, Duration duration)
    {
        redisTemplate.opsForValue().set(key, value, duration);
    }

    /**
     * Redis에서 키에 해당하는 값을 조회하는 메서드
     * 
     * @param key 조회할 값의 키
     * @return 키에 해당하는 값, 존재하지 않을 경우 null 반환
     */
    public T getValues(String key)
    {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * Redis에 특정 키가 존재하는지 확인하는 메서드
     * 
     * @param key 존재 여부를 확인할 키
     * @return 키가 존재하면 true, 그렇지 않으면 false
     */
    public boolean checkExistsValue(String key)
    {
        return redisTemplate.hasKey(key);
    }

    /**
     * Redis에서 키-값 쌍을 삭제하는 메서드
     * 
     * @param key 삭제할 키-값 쌍의 키
     */
    public void deleteValues(String key)
    {
        redisTemplate.delete(key);
    }


}
