package com.hbbhbank.moamoa.wallet.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WalletTransactionStatus {
  PENDING("PENDING", "거래 대기"),
  SUCCESS("SUCCESS", "거래 성공"),
  FAILED("FAILED", "거래 실패"),
  ;

  private final String code;
  private final String message;
}