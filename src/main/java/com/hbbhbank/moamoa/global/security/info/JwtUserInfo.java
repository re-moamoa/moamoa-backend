package com.hbbhbank.moamoa.global.security.info;

import com.hbbhbank.moamoa.user.domain.ERole;

public record JwtUserInfo(
  Long userId,
  ERole role
) {
}
