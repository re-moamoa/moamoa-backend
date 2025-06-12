package com.hbbhbank.moamoa.wallet.dto.response.wallet;

import com.hbbhbank.moamoa.wallet.domain.HwanbeeAccountLink;

public record BankAccountResponseDto(
  Long id,
  String accountNumber,
  String currency
) {
  public static BankAccountResponseDto from(HwanbeeAccountLink link) {
    return new BankAccountResponseDto(
      link.getId(),
      link.getHwanbeeBankAccountNumber(),
      link.getCurrencyCode()
    );
  }
}