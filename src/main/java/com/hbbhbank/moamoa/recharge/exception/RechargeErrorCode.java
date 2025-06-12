package com.hbbhbank.moamoa.recharge.exception;

import com.hbbhbank.moamoa.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RechargeErrorCode implements ErrorCode {

  DIRECT_RECHARGE_NOT_ALLOWED_FOR_FOREIGN_CURRENCY(HttpStatus.BAD_REQUEST, "RCG_001", "외화는 직접 충전이 불가능합니다."),
  EXCHANGE_RATE_FETCH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "RCG_002", "환율 정보를 불러오는 데 실패했습니다."),
  FEE_CALCULATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "RCG_003", "수수료 계산에 실패했습니다."),
  ACCOUNT_LINK_NOT_FOUND(HttpStatus.NOT_FOUND, "RCG_004", "연결된 계좌 정보를 찾을 수 없습니다."),
  CHARGE_AMOUNT_INVALID(HttpStatus.BAD_REQUEST, "RCG_005", "충전 금액이 유효하지 않습니다."),
  INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "RCG_006", "충전 금액은 만원 단위여야 합니다.")
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
