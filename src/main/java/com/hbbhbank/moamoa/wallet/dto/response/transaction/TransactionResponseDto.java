package com.hbbhbank.moamoa.wallet.dto.response.transaction;

import com.hbbhbank.moamoa.wallet.domain.ExternalWalletTransaction;
import com.hbbhbank.moamoa.wallet.domain.InternalWalletTransaction;
import com.hbbhbank.moamoa.wallet.domain.WalletTransactionStatus;
import com.hbbhbank.moamoa.wallet.domain.WalletTransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponseDto (
  Long id,
  String walletNumber,
  String counterWalletNumber,
  String currencyCode,
  WalletTransactionType type,
  WalletTransactionStatus status,
  BigDecimal amount,
  LocalDateTime transactedAt,
  boolean external
) {
  public static TransactionResponseDto from(InternalWalletTransaction tx) {
    return new TransactionResponseDto(
      tx.getId(),
      tx.getWallet().getWalletNumber(),
      tx.getCounterWallet() != null ? tx.getCounterWallet().getWalletNumber() : null,
      tx.getWallet().getCurrency().getName(),
      tx.getType(),
      tx.getStatus(),
      tx.getAmount(),
      tx.getTransactedAt(),
      false // 내부 거래이므로 external = false
    );
  }

  public static TransactionResponseDto from(ExternalWalletTransaction tx) {
    return new TransactionResponseDto(
      tx.getId(),
      tx.getWallet().getWalletNumber(),
      null,
      tx.getWallet().getCurrency().getCode(),
      tx.getType(),
      tx.getStatus(),
      tx.getAmount(),
      tx.getTransactedAt(),
      true
    );
  }

}

