package com.hbbhbank.moamoa.global.security.util;

import com.hbbhbank.moamoa.global.exception.BaseException;
import com.hbbhbank.moamoa.global.exception.GlobalErrorCode;
import com.hbbhbank.moamoa.global.security.principal.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

  public static void setAuthentication(Authentication authentication) {
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  public static Object getCurrentPrincipal() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      return null;
    }
    return authentication.getPrincipal();
  }

  public static Long getCurrentUserId() {
    Object principal = getCurrentPrincipal();
    if (principal instanceof UserPrincipal userPrincipal) {
      return userPrincipal.getUserId(); // → UserPrincipal에서 userId 추출
    }
    throw new BaseException(GlobalErrorCode.UNAUTHORIZED);
  }
}

