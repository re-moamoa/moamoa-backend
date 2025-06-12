package com.hbbhbank.moamoa.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SignUpRequestDto(
  @NotBlank(message = "이름은 필수로 입력해야 합니다.")
  String name,

  @Pattern(
    regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
    message = "유효한 이메일 형식이 아닙니다."
  )
  @NotBlank(message = "이메일은 필수로 입력해야 합니다.")
  String email, // 사용자 ID

  @Pattern(
    regexp = "^01([0|1|6|7|8|9])\\d{3,4}\\d{4}",
    message = "전화번호 형식에 맞게 입력해야 합니다."
  )
  @NotBlank(message = "전화번호는 필수로 입력해야 합니다.")
  String phoneNumber,

  @Pattern(regexp = "\\d{6}", message = "비밀번호는 6자리 숫자여야 합니다.")
  @NotBlank(message = "비밀번호는 필수로 입력해야 합니다.")
  String password,

  @Pattern(regexp = "\\d{6}", message = "비밀번호는 6자리 숫자여야 합니다.")
  @NotBlank(message = "비밀번호는 확인은 필수로 입력해야 합니다.")
  String passwordConfirm, // 비밀번호 확인

  String profileImage,

  Boolean serviceTermsAgreed, // 필수
  Boolean privacyPolicyAgreed, // 필수
  Boolean marketingAgreed // 선택
) {

  public void validate() {
    if (!serviceTermsAgreed){
      throw new IllegalArgumentException("서비스 이용 약관에 동의해야 가입이 가능합니다.");
    }

    if (!privacyPolicyAgreed) {
      throw new IllegalArgumentException("개인정보 처리방침에 동의해야 가입이 가능합니다.");
    }

    if (!password.equals(passwordConfirm)) {
      throw new IllegalArgumentException("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
    }

  }
}
