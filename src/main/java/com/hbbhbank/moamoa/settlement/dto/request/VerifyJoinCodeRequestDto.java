package com.hbbhbank.moamoa.settlement.dto.request;

import jakarta.validation.constraints.NotBlank;

public record VerifyJoinCodeRequestDto(
  @NotBlank(message = "초대 코드를 입력해주세요.")
  String joinCode
) {}