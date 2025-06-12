package com.hbbhbank.moamoa.external.dto.response.exchange;

public record ExchangeDealResponseDto(
  int status,
  String message,
  ExchangeDealDataDto data
) {
}
