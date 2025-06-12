package com.hbbhbank.moamoa.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ChangePasswordRequestDto(

  @Pattern(regexp = "\\d{6}", message = "비밀번호는 6자리 숫자여야 합니다.")
  @NotBlank
  String oldPassword,

  @Pattern(regexp = "\\d{6}", message = "비밀번호는 6자리 숫자여야 합니다.")
  @NotBlank
  String newPassword
) {
}
