package com.hbbhbank.moamoa.settlement.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SettlementStatus {
  BEFORE("BEFORE", "정산 전"),
  IN_PROGRESS("IN_PROGRESS", "정산 진행 중"),
  COMPLETE("COMPLETE", "정산 완료");

  private final String code;
  private final String message;
}
