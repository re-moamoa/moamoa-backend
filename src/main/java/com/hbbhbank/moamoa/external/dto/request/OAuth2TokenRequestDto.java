package com.hbbhbank.moamoa.external.dto.request;

import jakarta.validation.constraints.NotBlank;

public record OAuth2TokenRequestDto(
  @NotBlank(message = "인가 코드는 필수입니다.")
  String code
) {}
