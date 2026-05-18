package com.hbbhbank.moamoa.settlement.dto.request;

import com.hbbhbank.moamoa.settlement.domain.GroupStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateGroupStatusRequestDto(
  @NotNull(message = "변경할 그룹 상태는 필수입니다.")
  GroupStatus status
) {}
