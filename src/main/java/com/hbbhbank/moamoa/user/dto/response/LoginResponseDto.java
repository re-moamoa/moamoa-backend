package com.hbbhbank.moamoa.user.dto.response;

public record LoginResponseDto(
  Long userId,
  String accessToken,
  String refreshToken,
  String role
) {
}
