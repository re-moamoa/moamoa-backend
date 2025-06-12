package com.hbbhbank.moamoa.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoginRequestDto(
  @Pattern(
    regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
    message = "유효한 이메일 형식이 아닙니다."
  )
  @NotBlank(message = "이메일은 필수로 입력해야 합니다.")
  String email,

  @Pattern(regexp = "\\d{6}", message = "비밀번호는 6자리 숫자여야 합니다.")
  @NotBlank(message = "비밀번호는 필수로 입력해야 합니다.")
  String password
) {
}
