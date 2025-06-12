package com.hbbhbank.moamoa.user.domain;

import com.hbbhbank.moamoa.global.exception.BaseException;
import com.hbbhbank.moamoa.user.exception.UserErrorCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ProfileImage {
  IMAGE1("IMAGE1", "img_1"),
  IMAGE2("IMAGE2", "img_2"),
  IMAGE3("IMAGE3", "img_3"),
  IMAGE4("IMAGE4", "img_4");

  private final String key;   // DB 저장 값
  private final String value; // 프론트에 전달할 값

  public static ProfileImage fromKey(String key) {
    for (ProfileImage image : values()) {
      if (image.getKey().equals(key)) return image;
    }
    throw BaseException.type(UserErrorCode.NOT_FOUND_PROFILE_IMAGE);
  }

  public static ProfileImage from(String value) {
    for (ProfileImage image : values()) {
      if (image.getValue().equals(value)) return image;
    }
    throw BaseException.type(UserErrorCode.NOT_FOUND_PROFILE_IMAGE);
  }
}
