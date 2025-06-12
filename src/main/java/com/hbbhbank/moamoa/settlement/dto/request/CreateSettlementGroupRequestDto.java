package com.hbbhbank.moamoa.settlement.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateSettlementGroupRequestDto(
  @NotBlank(message = "그룹 이름은 필수입니다.")
  String groupName,

  @NotNull(message = "지갑 ID는 필수입니다.")
  Long walletId,

  @NotNull(message = "최대 멤버 수는 필수입니다.")
  @Min(value = 2, message = "최소 2명 이상이어야 합니다.")
  @Max(value = 10, message = "최대 10명까지 가능합니다.")
  Integer maxMembers
) {}