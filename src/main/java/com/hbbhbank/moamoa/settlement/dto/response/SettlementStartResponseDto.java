package com.hbbhbank.moamoa.settlement.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record SettlementStartResponseDto(
  int selectedMembers,
  BigDecimal totalAmount,
  BigDecimal baseAmount,                          // 기본 분담액
  BigDecimal remainder,                           // 나머지 금액
  List<MemberSettlementAmountDto> memberAmounts   // 멤버별 실제 분담액 (사다리타기 결과 포함)
) {}
