package com.rainbowforest.orderservice.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class CartRedisRepositoryImpl implements CartRedisRepository {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void addItemToCart(String key, Object item) {
        // Sử dụng OpsForSet vì Jedis cũ dùng sadd (Set add)
        redisTemplate.opsForSet().add(key, item);
    }

    @Override
    public Collection<Object> getCart(String key, Class type) {
        // Lấy tất cả thành viên của Set từ Redis
        Set<Object> members = redisTemplate.opsForSet().members(key);
        
        if (members == null) {
            return new ArrayList<>();
        }

        // Vì chúng ta đã cấu hình Jackson2JsonRedisSerializer trong RedisConfig,
        // Spring sẽ tự động convert JSON từ Redis thành Object cho chúng ta.
        return new ArrayList<>(members);
    }

    @Override
    public void deleteItemFromCart(String key, Object item) {
        // srem tương ứng với opsForSet().remove()
        redisTemplate.opsForSet().remove(key, item);
    }

    @Override
    public void deleteCart(String key) {
        // del tương ứng với delete()
        redisTemplate.delete(key);
    }
}