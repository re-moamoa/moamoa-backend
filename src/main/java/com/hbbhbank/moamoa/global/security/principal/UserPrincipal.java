package com.hbbhbank.moamoa.global.security.principal;

import com.hbbhbank.moamoa.user.domain.ERole;
import com.hbbhbank.moamoa.user.projection.UserSecurityForm;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter // 모든 필드에 대해 getter 자동 생성
@Builder // 빌더 패턴으로 객체 생성 가능하게 함
@RequiredArgsConstructor // final 필드만 포함한 생성자 자동 생성 (불변 객체 목적)

public class UserPrincipal implements UserDetails { // Spring Security의 인증 객체로 사용될 클래스
  private final Long userId; // 사용자의 식별자
  private final String password; // 비밀번호 (현재 null로 설정됨)
  private final ERole role; // 사용자의 권한(enum)
  private final Collection<? extends GrantedAuthority> authorities; // Spring Security에서 사용하는 권한 목록

  /**
   * 인증 객체(UserDetails)를 생성하는 정적 팩토리 메서드
   * DB 조회 결과(UserSecurityForm)를 바탕으로 UserPrincipal 객체 생성
   */
  public static UserPrincipal create(UserSecurityForm securityForm) {
    return UserPrincipal.builder()
      .userId(securityForm.getId()) // 사용자 ID 설정
      .role(securityForm.getRole()) // 사용자 권한 설정
      .authorities(Collections.singleton( // 권한을 Spring Security에서 인식 가능한 객체로 wrapping
        new SimpleGrantedAuthority(securityForm.getRole().getSecurityRole())))
      .build();
  }

  /**
   * 사용자의 권한 목록 반환
   * @return GrantedAuthority 타입의 권한 목록
   */
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return this.authorities;
  }

  /**
   * 비밀번호 반환
   * 현재는 사용하지 않기 때문에 null 반환 → OAuth 또는 JWT 기반 인증에서 종종 사용하지 않음
   */
  @Override
  public String getPassword() {
    return null;
  }

  /**
   * 사용자 이름 반환
   * 이 시스템에서는 userId(Long)을 문자열로 반환하여 사용자 식별자로 사용
   */
  @Override
  public String getUsername() {
    return this.userId.toString();
  }

  /**
   * 계정 만료 여부 반환 (true → 만료되지 않음)
   */
  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  /**
   * 계정 잠김 여부 반환 (true → 잠기지 않음)
   */
  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  /**
   * 자격 증명(비밀번호 등) 만료 여부 반환 (true → 유효함)
   */
  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  /**
   * 계정 활성화 여부 반환 (true → 활성 상태)
   */
  @Override
  public boolean isEnabled() {
    return true;
  }
}
