package com.hbbhbank.moamoa.global.config;

import com.hbbhbank.moamoa.external.dto.response.exchange.ExchangeRateResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.SimpleCacheResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@EnableCaching
public class RedisCacheConfig {

  @Bean
  public RedisCacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {

    log.info("RedisCacheConfig: redisCacheManager() called");

    RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
      .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
      .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
        new Jackson2JsonRedisSerializer<>(ExchangeRateResponseDto.class)
      ));

    Map<String, RedisCacheConfiguration> configMap = new HashMap<>();
    configMap.put("redisExchangeRates", config.entryTtl(Duration.ofHours(24)));

    return RedisCacheManager.builder(connectionFactory)
      .cacheDefaults(config)
      .withInitialCacheConfigurations(configMap)
      .build();
  }

  @Bean
  public CacheResolver redisCacheResolver(RedisCacheManager redisCacheManager) {
    return new SimpleCacheResolver(redisCacheManager);
  }
}