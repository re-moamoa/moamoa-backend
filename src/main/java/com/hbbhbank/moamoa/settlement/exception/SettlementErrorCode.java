package com.hbbhbank.moamoa.settlement.exception;

import com.hbbhbank.moamoa.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SettlementErrorCode implements ErrorCode {
  GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "SETTLEMENT_001", "정산 그룹이 존재하지 않습니다."),
  USER_ALREADY_TRANSFERRED(HttpStatus.BAD_REQUEST, "SETTLEMENT_002", "이미 송금이 완료된 사용자입니다."),
  SETTLEMENT_ALREADY_STARTED(HttpStatus.CONFLICT, "SETTLEMENT_003", "정산이 이미 시작되었거나 완료되었습니다."),
  INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "SETTLEMENT_004", "잔액이 부족합니다."),
  SETTLEMENT_NOT_IN_PROGRESS(HttpStatus.CONFLICT, "SETTLEMENT_005", "정산이 진행 중인 상태에서만 가능합니다."),
  SETTLEMENT_NOT_COMPLETE(HttpStatus.CONFLICT, "SETTLEMENT_006", "모든 멤버가 송금하지 않아 삭제할 수 없습니다."),
  MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "SETTLEMENT_007", "정산 그룹에 해당하는 멤버가 존재하지 않습니다."),
  UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "SETTLEMENT_008", "권한이 없는 접근입니다."),
  SHARE_PERIOD_NOT_DEFINED(HttpStatus.BAD_REQUEST, "SETTLEMENT_009", "정산 공유 기간이 정의되지 않았습니다."),
  ALREADY_INACTIVE(HttpStatus.CONFLICT, "SETTLEMENT_010", "이미 비활성화된 정산 그룹입니다."),
  ALREADY_ACTIVE(HttpStatus.CONFLICT, "SETTLEMENT_011", "이미 활성화된 정산 그룹입니다."),
  ACTIVE_SHARE_PERIOD_EXISTS(HttpStatus.CONFLICT, "SETTLEMENT_012", "활성화된 공유 기간이 존재합니다."),
  NO_ACCESS_TO_GROUP(HttpStatus.FORBIDDEN, "SETTLEMENT_013", "해당 정산 그룹에 접근할 수 없습니다."),
  SETTLEMENT_IN_PROGRESS(HttpStatus.CONFLICT, "SETTLEMENT_014", "정산이 진행 중입니다. 다른 작업을 수행할 수 없습니다"),
  MEMBER_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "SETTLEMENT_015", "정산 그룹의 최대 참여 인원 수를 초과했습니다."),
  SHARE_DISABLED(HttpStatus.BAD_REQUEST, "SETTLEMENT_016", "정산 공유가 비활성화되어 있습니다."),
  CANNOT_CANCEL_WHILE_IN_PROGRESS(HttpStatus.CONFLICT, "SETTLEMENT_017", "정산이 진행 중인 상태에서는 취소할 수 없습니다."),
  NO_ZERO_TO_SETTLE(HttpStatus.BAD_REQUEST, "SETTLEMENT_018", "정산 금액이 0원이거나, 정산할 멤버가 0명입니다."),
  WALLET_NOT_LINKED(HttpStatus.BAD_REQUEST, "SETTLEMENT_019", "지갑이 연결되어 있지 않습니다.")
  ;

  private final HttpStatus status;
  private final String errorCode;
  private final String message;

  @Override public HttpStatus status() { return status; }
  @Override public String errorCode() { return errorCode; }
  @Override public String message() { return message; }
}