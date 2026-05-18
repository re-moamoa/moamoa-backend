package com.hbbhbank.moamoa.settlement.dto.response;

import java.math.BigDecimal;

public record SettlementTransactionResponseDto(
  Long fromUserId,
  Long toUserId,
  BigDecimal amount,          // 총 정산 금액
  boolean isTransferred,      // 송금 여부
  Integer maxMembers,         // 정산 인원
  BigDecimal dividedAmount,   // 해당 멤버의 실제 분담액 (사다리타기 결과 반영)
  boolean isExtraPayer        // 사다리타기 당첨 여부
) {}
