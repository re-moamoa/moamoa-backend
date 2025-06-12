package com.hbbhbank.moamoa.settlement.dto.response;

import com.hbbhbank.moamoa.settlement.domain.SettlementSharePeriod;

import java.time.LocalDateTime;

public record SettlementSharePeriodDto(
  LocalDateTime startedAt,
  LocalDateTime stoppedAt
) {
  public static SettlementSharePeriodDto from(SettlementSharePeriod period) {
    return new SettlementSharePeriodDto(period.getStartedAt(), period.getStoppedAt());
  }
}
