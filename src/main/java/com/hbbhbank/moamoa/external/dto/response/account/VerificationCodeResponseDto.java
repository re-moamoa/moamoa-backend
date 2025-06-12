package com.hbbhbank.moamoa.external.dto.response.account;

public record VerificationCodeResponseDto(
  int status,
  String message,
  VerificationCodeDataDto data
) {}
