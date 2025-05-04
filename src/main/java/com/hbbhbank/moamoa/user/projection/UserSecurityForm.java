package com.hbbhbank.moamoa.user.projection;

import com.hbbhbank.moamoa.user.domain.ERole;

// 인증용 projection interface
public interface UserSecurityForm {
  Long getId();
  ERole getRole();
}