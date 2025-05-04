package com.hbbhbank.moamoa.global.security.provider;

import com.hbbhbank.moamoa.global.exception.BaseException;
import com.hbbhbank.moamoa.global.exception.GlobalErrorCode;
import com.hbbhbank.moamoa.global.security.info.JwtUserInfo;
import com.hbbhbank.moamoa.global.security.principal.UserPrincipal;
import com.hbbhbank.moamoa.global.security.service.CustomUserDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationProvider implements AuthenticationProvider {

  // 사용자 ID 기반으로 UserDetails를 불러오는 커스텀 서비스
  private final CustomUserDetailService customUserDetailService;

  /**
   * 실질적인 인증 로직이 구현된 메서드.
   * - JwtAuthenticationManager에서 이 메서드를 호출
   * - JWT의 payload 정보로부터 사용자 인증 수행
   */
  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    log.info("AuthenticationProvider 진입 성공");

    // Principal이 String이면 폼 로그인 또는 잘못된 요청으로 간주
    if (!authentication.getPrincipal().getClass().equals(String.class)) {
      log.info("로그인 한 사용자 검증 과정");

      // JWT 파싱 결과로 얻은 JwtUserInfo를 기반으로 인증 처리
      return authOfAfterLogin((JwtUserInfo) authentication.getPrincipal());
    } else {
      log.info("폼 또는 소셜 로그인 요청 감지 (비정상 요청)");
      throw new BaseException(GlobalErrorCode.VALIDATION_ERROR); // 커스텀 예외 발생
    }
  }

  /**
   * JWT 기반 인증 후 사용자 정보를 확인하고 SecurityContext에 저장할 Authentication 객체 생성
   */
  private Authentication authOfAfterLogin(JwtUserInfo userInfo) {
    // 사용자 ID를 기반으로 실제 UserDetails 객체 로드
    UserPrincipal userPrincipal = customUserDetailService.loadUserById(userInfo.userId());

    // 인증된 사용자 정보를 기반으로 Spring Security가 요구하는 인증 토큰 객체 반환
    return new UsernamePasswordAuthenticationToken(
      userPrincipal, // 인증된 사용자 정보
      null, // 비밀번호는 필요 없음 (이미 토큰으로 인증됨)
      userPrincipal.getAuthorities() // 권한 정보
    );
  }

  /**
   * 현재 Provider가 어떤 인증 타입을 처리할 수 있는지 명시
   */
  @Override
  public boolean supports(Class<?> authentication) {
    // UsernamePasswordAuthenticationToken 타입만 처리함
    return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
  }
}
