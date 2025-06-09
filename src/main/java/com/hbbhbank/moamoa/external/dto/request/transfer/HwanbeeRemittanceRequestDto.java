package com.hbbhbank.moamoa.external.dto.request.transfer;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record HwanbeeRemittanceRequestDto(
  String fromAccountNumber,
  String toAccountNumber,
  BigDecimal amount,
  String currency,
  String description,
  String partnerTransactionId
) {}
