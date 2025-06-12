package com.hbbhbank.moamoa.transfer.exception;

import com.hbbhbank.moamoa.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TransferErrorCode implements ErrorCode {

  WALLET_NOT_FOUND(HttpStatus.NOT_FOUND, "TRF_001", "지갑을 찾을 수 없습니다."),
  CURRENCY_MISMATCH(HttpStatus.BAD_REQUEST, "TRF_002", "같은 통화 지갑끼리만 송금할 수 있습니다."),
  INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "TRF_003", "지갑 잔액이 부족합니다."),
  CANNOT_TRANSFER_TO_SELF(HttpStatus.BAD_REQUEST, "TRF_004", "자신의 지갑으로는 송금할 수 없습니다."),
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
