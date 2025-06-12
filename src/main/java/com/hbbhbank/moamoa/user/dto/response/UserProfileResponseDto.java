package com.hbbhbank.moamoa.user.dto.response;

public record UserProfileResponseDto(
  String name,
  String profileImage
) {
  public static UserProfileResponseDto from(String name, String profileImage) {
    return new UserProfileResponseDto(name, profileImage);
  }
}
