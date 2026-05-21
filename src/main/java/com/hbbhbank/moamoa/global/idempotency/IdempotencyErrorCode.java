package com.hbbhbank.moamoa.global.idempotency;

import com.hbbhbank.moamoa.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum IdempotencyErrorCode implements ErrorCode {

  MISSING_IDEMPOTENCY_KEY(HttpStatus.BAD_REQUEST, "IDEMP_001", "Idempotency-Key 헤더가 필요합니다."),
  ;

  private final HttpStatus status;
  private final String errorCode;
  private final String message;

  @Override
  public HttpStatus status() {
    return status;
  }

  @Override
  public String errorCode() {
    return errorCode;
  }

  @Override
  public String message() {
    return message;
  }
}
