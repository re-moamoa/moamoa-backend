package com.hbbhbank.moamoa.external.dto.request.exchange;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;


public record ExchangeDealRequestDto(
  @NotBlank(message = "KRW 계좌 번호는 필수입니다.")
  String krwAccount,

  @NotBlank(message = "외화 계좌 번호는 필수입니다.")
  String fcyAccount,

  @NotNull(message = "환전할 원화 금액은 필수입니다.")
  @Positive(message = "금액은 양수여야 합니다.")
  Long krwAmount,

  @NotBlank(message = "환전 통화 코드는 필수입니다.")
  String currencyCode
) {
}
