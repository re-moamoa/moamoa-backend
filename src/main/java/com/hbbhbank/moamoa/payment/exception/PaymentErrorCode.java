package com.hbbhbank.moamoa.payment.exception;

import com.hbbhbank.moamoa.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PaymentErrorCode implements ErrorCode {

  FAILED_CREATE_QR(HttpStatus.INTERNAL_SERVER_ERROR, "QR_001", "QR 코드 생성에 실패했습니다."),
  INVALID_PARAMETER(HttpStatus.NOT_FOUND, "QR_002", "잘못된 파라미터입니다."),
  QR_EXPIRED(HttpStatus.NOT_FOUND, "QR_003", "QR 코드가 만료되었습니다."),
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
