package com.hbbhbank.moamoa.global.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.SimpleCacheResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@EnableCaching
@Slf4j
public class InMemoryCacheConfig {

  @Bean
  @Primary
  public CacheManager memoryCacheManager() {
    log.info("InMemoryCacheConfig: memoryCacheManager() called");
    return new ConcurrentMapCacheManager("memoryExchangeRates");
  }

  @Bean
  public CacheResolver memoryCacheResolver(CacheManager memoryCacheManager) {
    return new SimpleCacheResolver(memoryCacheManager);
  }
}
