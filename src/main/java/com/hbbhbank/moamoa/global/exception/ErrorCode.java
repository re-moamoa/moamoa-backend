package com.hbbhbank.moamoa.global.exception;

import org.springframework.http.HttpStatus;

/**
 * 모든 예외 코드가 반드시 제공해야 할 3가지 정보:
 * 1. HTTP 상태 코드
 * 2. 에러 코드 문자열 (예: "REQUEST_001")
 * 3. 사용자에게 보여줄 메시지
 */
public interface ErrorCode {

  // HTTP 응답 상태 코드 (예: 400, 404, 500 등)
  HttpStatus status();

  // 비즈니스 로직에서 사용할 식별자 형태의 에러 코드 문자열
  String errorCode();

  // 사용자에게 보여줄 메시지
  String message();
}
