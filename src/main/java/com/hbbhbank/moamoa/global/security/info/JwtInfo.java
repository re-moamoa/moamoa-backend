package com.hbbhbank.moamoa.global.security.info;

public record JwtInfo(
  String accessToken,
  String refreshToken,
  long refreshTokenExpirySeconds
) {
}
