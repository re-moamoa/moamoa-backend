package com.hbbhbank.moamoa.settlement.dto.response;

import java.math.BigDecimal;

public record SettlementStartResponseDto(
  int selectedMembers,
  BigDecimal totalAmount,
  BigDecimal perAmount
) {}
