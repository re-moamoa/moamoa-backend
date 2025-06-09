package com.hbbhbank.moamoa.external.scheduler;

import com.hbbhbank.moamoa.external.client.HwanbeeExchangeClient;
import com.hbbhbank.moamoa.external.dto.response.exchange.ExchangeRateResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangeRateScheduler {

  private final HwanbeeExchangeClient hwanbeeExchangeClient;
  private final RedisTemplate<String, Object> redisTemplate;

  private static final String CACHE_KEY = "exchangeRates::daily";

  /**
   * 매 정각마다 외부 API 호출 → Redis 캐시 갱신
   */
  @Scheduled(cron = "0 0 * * * *") // 매 정시 실행
  public void refreshExchangeRatesInRedis() {
    try {
      ExchangeRateResponseDto dto = hwanbeeExchangeClient.getAllExchangeRatesV3(); // API 호출
      redisTemplate.opsForValue().set(CACHE_KEY, dto, Duration.ofHours(1)); // TTL 1시간 설정
      log.info("[스케줄러] 환율 캐시 갱신 성공: {}", CACHE_KEY);
    } catch (Exception e) {
      log.error("[스케줄러] 환율 캐시 갱신 실패", e);
    }
  }
}