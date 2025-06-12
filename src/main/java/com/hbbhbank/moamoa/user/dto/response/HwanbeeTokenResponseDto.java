package com.hbbhbank.moamoa.user.dto.response;

public record HwanbeeTokenResponseDto(
  String accessToken,
  String refreshToken,
  Integer expiresIn
) {
}
