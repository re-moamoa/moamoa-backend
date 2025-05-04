package com.hbbhbank.moamoa.global.security.service;

import com.hbbhbank.moamoa.global.exception.BaseException;
import com.hbbhbank.moamoa.global.security.exception.AuthErrorCode;
import com.hbbhbank.moamoa.global.security.principal.UserPrincipal;
import com.hbbhbank.moamoa.global.security.repository.UserSecurityRepository;
import com.hbbhbank.moamoa.user.projection.UserSecurityForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

  private final UserSecurityRepository userSecurityRepository; // 사용자 인증 정보를 조회하기 위한 JPA Repository

  /**
   * 이메일(또는 사용자 이름) 기반으로 사용자를 조회하여 UserDetails 반환
   * Spring Security 내부 로그인 로직에서 사용됨
   */
  @Override
  public UserPrincipal loadUserByUsername(String email) throws UsernameNotFoundException {
    // 이메일을 기반으로 사용자 인증 정보를 projection 형태로 조회
    UserSecurityForm user = userSecurityRepository.findUserSecurityFromByEmail(email)
      // 사용자가 존재하지 않으면 Spring Security에서 정의한 예외를 던짐
      .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 이메일입니다."));

    // 조회된 정보를 기반으로 UserPrincipal 생성 (권한 포함)
    return UserPrincipal.create(user);
  }

  /**
   * JWT 인증 시 사용자 ID를 기준으로 사용자 인증 정보를 조회
   */
  public UserPrincipal loadUserById(Long id) {
    // 사용자 ID를 기반으로 projection 조회
    UserSecurityForm userSecurityForm = userSecurityRepository.findUserSecurityFromById(id)
      // 존재하지 않으면 커스텀 예외 던짐 (도메인에 맞는 예외 처리 가능)
      .orElseThrow(() -> new BaseException(AuthErrorCode.USER_NOT_FOUND));

    log.info("user id 기반 조회 성공");

    // 인증 객체 생성
    return UserPrincipal.create(userSecurityForm);
  }
}
