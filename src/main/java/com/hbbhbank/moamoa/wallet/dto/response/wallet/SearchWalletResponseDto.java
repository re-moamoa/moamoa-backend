package com.hbbhbank.moamoa.wallet.dto.response.wallet;

import com.hbbhbank.moamoa.wallet.domain.Wallet;

import java.math.BigDecimal;

public record SearchWalletResponseDto(
  Long walletId,
  String currencyCode,
  String currencyName,
  BigDecimal balance,
  String walletNumber,
  String userName
) {
  public static SearchWalletResponseDto from(Wallet w) {
    return new SearchWalletResponseDto(
      w.getId(),
      w.getCurrency().getCode(),
      w.getCurrency().getName(),
      w.getBalance(),
      w.getWalletNumber(),
      w.getUser().getName()
    );
  }
}

