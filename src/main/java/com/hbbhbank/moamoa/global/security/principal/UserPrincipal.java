package com.hbbhbank.moamoa.global.security.principal;

import com.hbbhbank.moamoa.user.domain.ERole;
import com.hbbhbank.moamoa.global.security.dto.UserSecurityForm;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
@Builder
@RequiredArgsConstructor
public class UserPrincipal implements UserDetails {

  private final Long userId;
  private final ERole role;
  private final Collection<? extends GrantedAuthority> authorities;


  public static UserPrincipal create(UserSecurityForm securityForm) {
    String authority = "ROLE_" + securityForm.getRole().name(); // ROLE_USER
    return UserPrincipal.builder()
      .userId(securityForm.getId())
      .role(securityForm.getRole())
      .authorities(Collections.singleton(new SimpleGrantedAuthority(authority)))
      .build();
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public String getPassword() {
    return null;
  }

  @Override
  public String getUsername() {
    return String.valueOf(userId);
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}
