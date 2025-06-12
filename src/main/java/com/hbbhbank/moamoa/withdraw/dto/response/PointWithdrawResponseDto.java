package com.hbbhbank.moamoa.withdraw.dto.response;

import java.math.BigDecimal;

public record PointWithdrawResponseDto(
  Long transactionId,
  String fromWalletNumber,
  String toBankAccount,
  BigDecimal amount,
  String currency
) {}
