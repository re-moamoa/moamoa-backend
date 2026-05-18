package com.hbbhbank.moamoa.settlement.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record SettlementHistoryResponseDto(
  int settlementRound,
  BigDecimal totalAmount,
  BigDecimal dividedAmount,
  int memberCount,
  LocalDateTime completedAt,
  List<SettlementHistoryDetailDto> details
) {

  public record SettlementHistoryDetailDto(
    Long fromUserId,
    BigDecimal amount,
    boolean isTransferred,
    LocalDateTime transferredAt
  ) {}
}
