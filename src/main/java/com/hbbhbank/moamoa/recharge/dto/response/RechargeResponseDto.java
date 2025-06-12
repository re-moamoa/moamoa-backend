package com.hbbhbank.moamoa.recharge.dto.response;

import java.math.BigDecimal;

public record RechargeResponseDto(
  String walletNumber,
  BigDecimal amount,
  String currency
) {
}
