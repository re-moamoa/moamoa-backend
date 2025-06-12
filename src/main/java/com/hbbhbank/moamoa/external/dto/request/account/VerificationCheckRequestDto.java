package com.hbbhbank.moamoa.external.dto.request.account;

public record VerificationCheckRequestDto(
  String transactionId,
  String inputCode
) {
  public static VerificationCheckRequestDto of(VerificationCheckRequestDto dto) {
    return new VerificationCheckRequestDto(
      dto.transactionId(),
      dto.inputCode()
    );
  }
}
