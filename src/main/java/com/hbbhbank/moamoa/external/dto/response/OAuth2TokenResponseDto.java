package com.hbbhbank.moamoa.external.dto.response;

public record OAuth2TokenResponseDto(
  String accessToken,
  String refreshToken,
  int expiresIn
) {}