package com.hbbhbank.moamoa.user.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ERole {
  USER("USER", "ROLE_USER");

  private final String role;         // 도메인 용도 (예: DB 저장용, 내부 로직용)
  private final String securityRole; // Spring Security 권한 체크용 ("ROLE_" prefix 필수)
}
