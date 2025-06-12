package com.hbbhbank.moamoa.recharge.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RechargeRequestDto(
  @NotBlank String currencyCode,
  @NotBlank String hwanbeeAccountNumber,
  @NotNull @DecimalMin("0.01") BigDecimal amount,
  @NotBlank String password
) {}
