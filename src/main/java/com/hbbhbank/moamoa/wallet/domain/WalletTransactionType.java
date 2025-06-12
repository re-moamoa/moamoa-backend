package com.hbbhbank.moamoa.wallet.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WalletTransactionType {

  // InternalWalletTransaction
  QR_PAYMENT("QR_PAYMENT", "QR 결제"),
  TRANSFER_OUT("TRANSFER_OUT", "송금 보내기"),
  TRANSFER_IN("TRANSFER_IN", "송금 받기"),
  SETTLEMENT_SEND("SETTLEMENT_SEND", "정산 하기"),
  SETTLEMENT_RECEIVE("SETTLEMENT_RECEIVE", "정산 받기"),

  // ExternalWalletTransaction
  CHARGE("CHARGE", "충전"),
  WITHDRAWAL("WITHDRAWAL","환불"),
  ;

  private final String code;
  private final String message;

  // 입금
  public boolean isIncomeType() {
    return switch (this) {
      case CHARGE, TRANSFER_IN, SETTLEMENT_RECEIVE-> true;
      default -> false;
    };
  }

  // 출금
  public boolean isExpenseType() {
    return switch (this) {
      case QR_PAYMENT, WITHDRAWAL, TRANSFER_OUT, SETTLEMENT_SEND -> true;
      default -> false;
    };
  }
}