package com.hbbhbank.moamoa.global.exception;

public record CustomErrorResponse(
  int status,
  String errorCode,
  String message
) {

  // 에러코드에 정의된 메시지(errorCode.message())를 그대로 사용
  public static CustomErrorResponse from(ErrorCode errorCode) {
    return new CustomErrorResponse(
      errorCode.status().value(),
      errorCode.errorCode(),
      errorCode.message()
    );
  }

  // 에러코드는 유지하되, 메시지를 재정의할 수 있도록 함
  public static CustomErrorResponse of(ErrorCode errorCode, String message) {
    return new CustomErrorResponse(
      errorCode.status().value(),
      errorCode.errorCode(),
      message
    );
  }
}
