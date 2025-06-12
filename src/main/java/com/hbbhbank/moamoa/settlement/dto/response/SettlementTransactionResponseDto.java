package com.hbbhbank.moamoa.settlement.dto.response;

import java.math.BigDecimal;

public record SettlementTransactionResponseDto(
  Long fromUserId,
  Long toUserId,
  BigDecimal amount,   // 총 정산 금액
  boolean isTransferred, // 송금 여부
  Integer maxMembers, // 정산 인원
  BigDecimal dividedAmount // 1인당 정산 금액
) {}
