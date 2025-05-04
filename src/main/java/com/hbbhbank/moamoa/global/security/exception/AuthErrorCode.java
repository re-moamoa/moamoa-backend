package com.hbbhbank.moamoa.global.security.exception;

import com.hbbhbank.moamoa.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import lombok.AllArgsConstructor;

/**
 * 인증 및 토큰 처리 관련 에러 코드
 */
@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

  // JWT 파싱 및 키 오류
  CANNOT_JSON_PROCESS(HttpStatus.BAD_REQUEST, "AUTH_001", "JSON을 직렬화 혹은 역직렬화할 수 없습니다."),
  NOT_MATCHED_PUBLIC_KEY(HttpStatus.UNAUTHORIZED, "AUTH_002", "적절한 공개키를 찾을 수 없습니다."),
  NOT_FOUND_ALGORITHM(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH_003", "지원하지 않는 공개키 알고리즘입니다."),
  NOT_SUPPORTED_ALGORITHM(HttpStatus.BAD_REQUEST, "AUTH_004", "Spring Security에서 지원하지 않는 공개키 알고리즘입니다."),
  FAILED_TO_READ_PRIVATE_KEY(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH_006", "개인 키 정보를 읽는 데 실패했습니다."),
  PEM_PARSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH_007", "PEM 데이터 처리 중 오류가 발생했습니다."),
  FILE_READ_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH_008", "개인 키 파일을 읽는 중 오류가 발생했습니다."),

  // 토큰 자체 문제
  MISSING_TOKEN(HttpStatus.BAD_REQUEST, "AUTH_009", "토큰이 비어있습니다."),
  INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_005", "Refresh Token이 유효하지 않습니다."),
  EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_010", "만료된 토큰입니다."),
  TOKEN_MALFORMED(HttpStatus.UNAUTHORIZED, "AUTH_011", "토큰 포맷이 올바르지 않습니다."),
  UNSUPPORTED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_012", "지원하지 않는 토큰입니다."),
  TOKEN_SIGNATURE_INVALID(HttpStatus.UNAUTHORIZED, "AUTH_013", "토큰 서명이 유효하지 않습니다."),

  // 사용자 인증/인가
  INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_020", "아이디 또는 비밀번호가 올바르지 않습니다."),
  UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "AUTH_021", "로그인이 필요한 요청입니다."),
  FORBIDDEN(HttpStatus.FORBIDDEN, "AUTH_022", "권한이 없습니다."),
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH_023", "해당 사용자를 찾을 수 없습니다.");

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
