package com.hbbhbank.moamoa.global.security.dto;

import com.hbbhbank.moamoa.user.domain.ERole;

// 인증용 projection interface
public interface UserSecurityForm {
  Long getId();
  ERole getRole();
}