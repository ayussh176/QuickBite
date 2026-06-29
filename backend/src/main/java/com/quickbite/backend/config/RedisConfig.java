package com.quickbite.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis configuration with JSON serialization and Spring caching.
 */
@Configuration
@EnableCaching
public class RedisConfig {

    public static class PageDeserializer extends com.fasterxml.jackson.databind.JsonDeserializer<org.springframework.data.domain.Page<?>> {
        @Override
        public org.springframework.data.domain.Page<?> deserialize(com.fasterxml.jackson.core.JsonParser p, com.fasterxml.jackson.databind.DeserializationContext ctxt) throws java.io.IOException {
            com.fasterxml.jackson.databind.JsonNode node = p.getCodec().readTree(p);
            com.fasterxml.jackson.databind.JsonNode contentNode = node.get("content");
            java.util.List<Object> content = new java.util.ArrayList<>();
            if (contentNode != null && contentNode.isArray()) {
                for (com.fasterxml.jackson.databind.JsonNode item : contentNode) {
                    content.add(p.getCodec().treeToValue(item, Object.class));
                }
            }
            long total = node.has("totalElements") ? node.get("totalElements").asLong() : content.size();
            int pageNumber = node.has("number") ? node.get("number").asInt() : 0;
            int pageSize = node.has("size") ? node.get("size").asInt() : Math.max(1, content.size());
            return new org.springframework.data.domain.PageImpl<>(content, org.springframework.data.domain.PageRequest.of(pageNumber, pageSize), total);
        }
    }

    private GenericJackson2JsonRedisSerializer jsonSerializer() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        
        com.fasterxml.jackson.databind.module.SimpleModule pageModule = new com.fasterxml.jackson.databind.module.SimpleModule();
        pageModule.addDeserializer(org.springframework.data.domain.Page.class, new PageDeserializer());
        pageModule.addDeserializer(org.springframework.data.domain.PageImpl.class, (com.fasterxml.jackson.databind.JsonDeserializer) new PageDeserializer());
        mapper.registerModule(pageModule);

        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.disable(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        // Enable default typing
        com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator ptv = 
                com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator.instance;
        mapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL, com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY);

        return new GenericJackson2JsonRedisSerializer(mapper);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Use String serializer for keys
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // Use JSON serializer for values
        GenericJackson2JsonRedisSerializer jsonSerializer = jsonSerializer();
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        GenericJackson2JsonRedisSerializer jsonSerializer = jsonSerializer();

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10)) // Default TTL 10 mins
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer))
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .withCacheConfiguration("restaurants", config.entryTtl(Duration.ofMinutes(30)))
                .withCacheConfiguration("menu", config.entryTtl(Duration.ofMinutes(15)))
                .withCacheConfiguration("categories", config.entryTtl(Duration.ofHours(2)))
                .withCacheConfiguration("wallets", config.entryTtl(Duration.ofMinutes(5)))
                .withCacheConfiguration("coupons", config.entryTtl(Duration.ofHours(1)))
                .build();
    }
}
