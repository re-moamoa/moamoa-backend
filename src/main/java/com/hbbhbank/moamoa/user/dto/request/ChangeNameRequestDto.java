package com.hbbhbank.moamoa.user.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ChangeNameRequestDto(
  @NotBlank String name
) {
}
