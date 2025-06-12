package com.hbbhbank.moamoa.external.dto.response.exchange;

public record SingleExchangeRateResponseDto(
  int status,
  String message,
  SingleExchangeRateDataDto data
) {
}
