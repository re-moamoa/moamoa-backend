package com.hbbhbank.moamoa.external.dto.response.transfer;

import java.math.BigDecimal;

public record HwanbeeRemittanceResponseDto(
  int status,
  String message,
  RemittanceData data
) {
  public record RemittanceData(
    String transactionId,
    String transferredAt
  ) {}
}