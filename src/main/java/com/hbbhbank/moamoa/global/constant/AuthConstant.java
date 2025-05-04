package com.hbbhbank.moamoa.global.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuthConstant {

  public static final String CLAIM_USER_ID = "uuid";
  public static final String CLAIM_USER_ROLE = "role";

  public static final String PREFIX_BEARER = "Bearer ";
  public static final String PREFIX_AUTH = "Authorization";

  public static final List<String> NOT_NEED_AUTH = List.of(
    "/api/v1/auth/signup",
    "/api/v1/auth/login"
  );
}

