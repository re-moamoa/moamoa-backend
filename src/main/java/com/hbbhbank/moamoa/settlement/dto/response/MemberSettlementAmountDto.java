package com.hbbhbank.moamoa.settlement.dto.response;

import java.math.BigDecimal;

public record MemberSettlementAmountDto(
  Long userId,
  BigDecimal amount,      // 실제 분담액
  boolean isExtraPayer    // 사다리타기 당첨 여부 (나머지 추가 부담)
) {}
