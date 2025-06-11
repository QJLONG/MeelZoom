package com.nemo.mealzoom.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class RedisConfig {

    /**
     * 配置RedisTemplate序列化器
     * @param factory
     * @return
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // 创建并配置Jackson序列化器，用于对value处理（因为value可能是对象）
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        // 创建并配置mapper：用于映射json与object的关系
        ObjectMapper mapper = new ObjectMapper();
        // 设置序列化范围:
        // PropertyAccessor.ALL 表示对所有类型的属性（如字段、方法等）进行配置。
        // JsonAutoDetect.Visibility.ANY 表示任何访问级别的属性（包括私有属性）都可以被序列化和反序列化。这样设置后，Jackson 可以处理类中所有的属性，而不仅仅是有 getter 和 setter 方法的属性。
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.activateDefaultTyping(mapper.getPolymorphicTypeValidator(), ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(mapper);

        // 创建stringRedis序列化器，用于处理key
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        // template.setKeySerializer(stringRedisSerializer);
        // template.setHashKeySerializer(stringRedisSerializer);
        // template.setValueSerializer(jackson2JsonRedisSerializer);
        // template.setHashValueSerializer(jackson2JsonRedisSerializer);
        template.afterPropertiesSet(); // 对template的属性进行初始化

        return template;
    }
}
