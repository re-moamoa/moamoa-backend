package com.hbbhbank.moamoa.wallet.domain;

import com.hbbhbank.moamoa.global.exception.BaseException;
import com.hbbhbank.moamoa.wallet.exception.WalletErrorCode;

import java.math.BigDecimal;

public enum CurrencyUnit {
  KRW("KRW", BigDecimal.valueOf(10_000), BigDecimal.ONE),
  USD("USD", BigDecimal.valueOf(10), new BigDecimal("0.01")),
  EUR("EUR", BigDecimal.valueOf(10), new BigDecimal("0.01")),
  JPY("JPY", BigDecimal.valueOf(1_000), BigDecimal.ONE),
  CNY("CNY", BigDecimal.valueOf(50), new BigDecimal("0.01")),
  VND("VND", BigDecimal.valueOf(200_000), BigDecimal.ONE),
  INR("INR", BigDecimal.valueOf(800), BigDecimal.ONE);

  private final String currencyCode;
  private final BigDecimal unitAmount;
  private final BigDecimal smallestUnit; // 정산 나머지 분배 시 최소 단위

  CurrencyUnit(String currencyCode, BigDecimal unitAmount, BigDecimal smallestUnit) {
    this.currencyCode = currencyCode;
    this.unitAmount = unitAmount;
    this.smallestUnit = smallestUnit;
  }

  public BigDecimal getUnitAmount() {
    return unitAmount;
  }

  public BigDecimal getSmallestUnit() {
    return smallestUnit;
  }

  public static CurrencyUnit fromCode(String code) {
    for (CurrencyUnit unit : values()) {
      if (unit.currencyCode.equalsIgnoreCase(code)) {
        return unit;
      }
    }
    throw BaseException.type(WalletErrorCode.CURRENCY_CODE_NOT_FOUND);
  }
}