package com.hbbhbank.moamoa.external.dto.response.account;

import com.fasterxml.jackson.annotation.JsonInclude;

// 최상위 응답 record
@JsonInclude(JsonInclude.Include.NON_NULL)
public record VerificationCheckResponseDto(
  int status,
  String message,
  VerificationAccountDataDto data
) {}
