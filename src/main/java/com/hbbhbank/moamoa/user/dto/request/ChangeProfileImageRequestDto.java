package com.hbbhbank.moamoa.user.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ChangeProfileImageRequestDto(
  @NotBlank(message = "프로필 이미지를 선택해주세요.")
  String profileImage
) {}
