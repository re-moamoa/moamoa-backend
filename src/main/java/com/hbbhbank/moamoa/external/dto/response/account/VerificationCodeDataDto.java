package com.hbbhbank.moamoa.external.dto.response.account;

public record VerificationCodeDataDto(
  String transactionId,
  String status,
  String message
) {}
