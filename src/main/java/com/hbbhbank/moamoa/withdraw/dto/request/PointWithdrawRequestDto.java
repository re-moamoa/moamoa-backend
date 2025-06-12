package com.hbbhbank.moamoa.withdraw.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PointWithdrawRequestDto(
  @NotBlank String bankAccount,
  @NotNull @DecimalMin("0.01") BigDecimal amount,
  @NotBlank String password
) {}