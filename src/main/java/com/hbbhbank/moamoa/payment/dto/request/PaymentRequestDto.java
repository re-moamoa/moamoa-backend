package com.hbbhbank.moamoa.payment.dto.request;

import com.hbbhbank.moamoa.global.exception.BaseException;
import com.hbbhbank.moamoa.payment.exception.PaymentErrorCode;

import java.math.BigDecimal;

public record PaymentRequestDto(
  Long sellerWalletId,
  String currencyCode,
  BigDecimal amount
) {
  public void validate() {
    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new BaseException(PaymentErrorCode.INVALID_PARAMETER);
    }
  }
}
