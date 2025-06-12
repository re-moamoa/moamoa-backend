package com.hbbhbank.moamoa.global.exception;

import lombok.Getter;

/**
 * 애플리케이션 전반에서 공통으로 사용할 커스텀 런타임 예외 클래스
 */
@Getter
public class BaseException extends RuntimeException {
  private final ErrorCode code;

  public BaseException(ErrorCode code) {
    super(code.message()); // 부모 클래스인 RuntimeException의 메시지로 세팅
    this.code = code;
  }

  // 가독성 향상용 메서드
  public static BaseException type(ErrorCode code) {
    return new BaseException(code);
  }
}
