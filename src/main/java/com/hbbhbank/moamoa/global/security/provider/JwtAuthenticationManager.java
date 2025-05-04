package com.hbbhbank.moamoa.global.security.provider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationManager implements AuthenticationManager {

  // 실제 인증 로직을 담당하는 커스텀 Provider (비밀번호 DB 비교 대신 JWT 검증을 수행)
  private final JwtAuthenticationProvider jwtAuthenticationProvider;

  /**
   * Spring Security의 인증 필터가 호출하는 핵심 메서드.
   * - JwtAuthenticationFilter에서 Authentication 객체를 넘기면 여기서 처리됨
   * - 이 메서드는 JwtAuthenticationProvider의 authenticate()를 위임 호출
   */
  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    log.info("AuthenticationManager 진입"); // 인증 시도 로그 기록
    return jwtAuthenticationProvider.authenticate(authentication); // 실제 인증은 Provider에서 수행
  }
}