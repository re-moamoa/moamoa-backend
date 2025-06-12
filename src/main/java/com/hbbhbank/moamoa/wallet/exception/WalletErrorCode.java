package com.hbbhbank.moamoa.wallet.exception;

import com.hbbhbank.moamoa.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum WalletErrorCode implements ErrorCode {
  NOT_FOUND_WALLET(HttpStatus.NOT_FOUND, "WALLET_001", "존재하지 않는 지갑입니다."),
  DUPLICATE_WALLET(HttpStatus.CONFLICT, "WALLET_002", "이미 해당 통화의 지갑이 존재합니다."),
  CURRENCY_CODE_NOT_FOUND(HttpStatus.NOT_FOUND, "WALLET_003", "존재하지 않는 통화 코드입니다."),
  WALLET_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "WALLET_004", "지갑 생성에 실패했습니다."),
  INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "WALLET_005", "잔액이 부족합니다."),
  INVALID_TRANSACTION_AMOUNT(HttpStatus.BAD_REQUEST, "WALLET_006", "유효하지 않은 거래 금액입니다."),
  INVALID_TRANSACTION_TYPE(HttpStatus.BAD_REQUEST, "WALLET_007", "유효하지 않은 거래 유형입니다."),
  ACCOUNT_LINK_NOT_FOUND(HttpStatus.NOT_FOUND, "WALLET_009", "계좌 연결 정보를 찾을 수 없습니다."),
  TRANSACTION_NOT_FOUND(HttpStatus.NOT_FOUND, "WALLET_010", "거래 내역을 찾을 수 없습니다."),
  USER_ACCOUNT_LINK_NOT_MATCHED(HttpStatus.NOT_FOUND, "WALLET_011","사용자와 계좌 연결 정보가 일치하지 않습니다."),
  USER_ACCOUNT_LINK_CURRENCY_MISMATCH(HttpStatus.NOT_FOUND, "WALLET_012","사용자와 계좌 연결 정보의 통화가 일치하지 않습니다."),
  FAIL_VERIFICATION(HttpStatus.BAD_REQUEST, "WALLET_013", "인증에 실패했습니다."),
  CURRENCY_MISMATCH(HttpStatus.BAD_REQUEST, "WALLET_014", "지갑의 통화와 요청된 통화가 일치하지 않습니다."),
  NOT_FOUND_VERIFICATION_REQUEST(HttpStatus.NOT_FOUND, "WALLET_015", "인증 요청을 찾을 수 없습니다."),
  ;

  private final HttpStatus status;
  private final String errorCode;
  private final String message;

  @Override
  public HttpStatus status() { return status; }

  @Override
  public String errorCode() { return errorCode; }

  @Override
  public String message() { return message; }
}