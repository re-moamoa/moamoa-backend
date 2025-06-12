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

  private final CustomUserDetailService customUserDetailService;

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    log.info("AuthenticationProvider 진입 성공");

    if (!(authentication.getPrincipal() instanceof JwtUserInfo)) {
      log.warn("비정상 요청 감지: principal이 JwtUserInfo가 아님");
      throw new BaseException(GlobalErrorCode.VALIDATION_ERROR);
    }

    JwtUserInfo userInfo = (JwtUserInfo) authentication.getPrincipal();
    UserPrincipal userPrincipal = customUserDetailService.loadUserById(userInfo.userId());

    log.info("인증 객체 생성됨: id={}, role={}, authorities={}",
      userPrincipal.getUserId(),
      userPrincipal.getRole(),
      userPrincipal.getAuthorities());

    return new UsernamePasswordAuthenticationToken(
      userPrincipal,
      null,
      userPrincipal.getAuthorities()
    );
  }

  private Authentication authOfAfterLogin(JwtUserInfo userInfo) {
    UserPrincipal userPrincipal = customUserDetailService.loadUserById(userInfo.userId());

    return new UsernamePasswordAuthenticationToken(
      userPrincipal,
      null,
      userPrincipal.getAuthorities()
    );
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
  }
}
