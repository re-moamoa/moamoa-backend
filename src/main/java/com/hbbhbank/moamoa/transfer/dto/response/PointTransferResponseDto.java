package com.hbbhbank.moamoa.transfer.dto.response;

import java.math.BigDecimal;

public record PointTransferResponseDto(
  String toUserName,
  String toWalletNumber,
  BigDecimal amount,
  String currency
) {}