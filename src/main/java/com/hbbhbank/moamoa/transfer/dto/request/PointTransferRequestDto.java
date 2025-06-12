package com.hbbhbank.moamoa.transfer.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PointTransferRequestDto(
  @NotBlank String fromWalletNumber,
  @NotBlank String toWalletNumber,
  @NotNull @DecimalMin("0.01") BigDecimal amount
) {}
