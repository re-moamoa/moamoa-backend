package com.hbbhbank.moamoa.external.dto.request.account;

public record VerificationCodeRequestDto(
  String accountNumber,
  String currencyCode
) {
  public static VerificationCodeRequestDto of(VerificationCodeRequestDto origin) {
    return new VerificationCodeRequestDto(
      origin.accountNumber(),
      origin.currencyCode()
    );
  }
}
