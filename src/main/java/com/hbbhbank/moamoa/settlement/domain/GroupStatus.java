package com.hbbhbank.moamoa.settlement.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GroupStatus {
  ACTIVE("ACTIVE", "활성화됨"),
  INACTIVE("INACTIVE", "비활성화됨"),
  ;

  private final String code;
  private final String message;
}
