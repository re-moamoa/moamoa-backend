package com.hbbhbank.moamoa.wallet.dto.request.wallet;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SearchWalletRequestDto(

  @NotBlank(message = "통화 코드는 필수입니다.")
  @Pattern(
    regexp = "^(KRW|USD|JPY|EUR|VND|CNY|INR)$",
    message = "지원하지 않는 통화입니다."
  )
  String currencyCode
) {}
