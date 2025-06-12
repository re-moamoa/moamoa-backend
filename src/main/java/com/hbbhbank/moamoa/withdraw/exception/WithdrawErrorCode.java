package com.hbbhbank.moamoa.withdraw.exception;

import com.hbbhbank.moamoa.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum WithdrawErrorCode implements ErrorCode {

  WALLET_NOT_FOUND(HttpStatus.NOT_FOUND, "WDR_001", "출금할 지갑 정보를 찾을 수 없습니다."),
  INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "WDR_002", "출금할 지갑에 잔액이 부족합니다."),
  ACCOUNT_LINK_NOT_FOUND(HttpStatus.NOT_FOUND, "WDR_003", "연결된 외부 계좌 정보를 찾을 수 없습니다."),
  TRANSFER_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "WDR_004", "출금 처리 중 오류가 발생했습니다."),
  INVALID_WITHDRAWAL_AMOUNT(HttpStatus.BAD_REQUEST, "WDR_005", "출금 금액이 유효하지 않습니다.")
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
