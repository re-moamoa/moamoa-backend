package com.hbbhbank.moamoa.global.idempotency;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 만료된 멱등성 키를 주기적으로 정리하는 스케줄러.
 *
 * <p>멱등성 키는 클라이언트 재시도 시나리오를 위해 일정 시간 보관되며,
 * 24시간이 지난 키는 더 이상 재시도가 발생하지 않는다고 판단하여 삭제한다.</p>
 *
 * <p>매일 새벽 3시에 실행되어 DB 부하가 적은 시간대에 정리를 수행한다.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IdempotencyKeyCleanupScheduler {

  private static final int RETENTION_HOURS = 24;

  private final IdempotencyKeyRepository idempotencyKeyRepository;

  @Scheduled(cron = "0 0 3 * * *")
  @Transactional
  public void cleanupExpiredKeys() {
    LocalDateTime threshold = LocalDateTime.now().minusHours(RETENTION_HOURS);
    int deletedCount = idempotencyKeyRepository.deleteByCreatedAtBefore(threshold);
    log.info("만료된 멱등성 키 정리 완료: 삭제 건수={}, 기준시각={}", deletedCount, threshold);
  }
}
