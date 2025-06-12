package com.hbbhbank.moamoa.user.dto.response;

import com.hbbhbank.moamoa.user.domain.User;

public record SignUpResponseDto(
  Long userId,
  String email,
  String name,
  String profileImage
) {
  public static SignUpResponseDto from(User user) {
    return new SignUpResponseDto(
      user.getId(),
      user.getEmail(),
      user.getName(),
      user.getProfileImage().getValue()
    );
  }
}
