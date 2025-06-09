package com.hbbhbank.moamoa.external.scheduler;

import com.hbbhbank.moamoa.external.client.HwanbeeExchangeClient;
import com.hbbhbank.moamoa.external.dto.response.exchange.ExchangeRateResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangeRateScheduler {

  private final HwanbeeExchangeClient hwanbeeExchangeClient;

  /**
   * 인메모리 캐시를 선제적으로 채우기 위한 스케줄러
   * - @Cacheable 이 붙은 메서드를 강제로 호출
   */
  @Scheduled(cron = "0 0 * * * *") // 매 정각
  public void preloadExchangeRatesToMemoryCache() {
    try {
      ExchangeRateResponseDto dto = hwanbeeExchangeClient.getAllExchangeRatesV3(); // @Cacheable method
      log.info("[스케줄러] 인메모리 환율 캐시 갱신 완료 (dataSize={})",
        dto.data() != null ? dto.data().size() : -1);
    } catch (Exception e) {
      log.error("[스케줄러] 인메모리 환율 캐시 갱신 실패", e);
    }
  }
}
