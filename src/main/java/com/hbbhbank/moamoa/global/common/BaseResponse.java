package com.hbbhbank.moamoa.global.common;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.hbbhbank.moamoa.global.exception.CustomErrorResponse;
import com.hbbhbank.moamoa.global.exception.GlobalErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * API 응답을 위한 기본 클래스
 * 일관성을 확보하기 위해 작성
 */
@Getter
@AllArgsConstructor
@JsonPropertyOrder({"errorCode", "message", "result"}) // JSON 직렬화 시 필드 순서를 지정 (에러 코드 → 메시지 → 결과 순) - 없으면 순서 랜덤
public class BaseResponse<T> {
  private final String errorCode; // 에러 코드 (null이면 성공, 값이 있으면 실패)
  private final String message; // 메시지 (성공/실패 여부에 대한 설명)
  private T result; // 실제 결과 데이터 (T는 어떤 타입도 될 수 있음)

  public static <T> BaseResponse<T> success(final T data) {
    return new BaseResponse<>(null, "SUCCESS", data); // 에러코드 없이, 메시지는 "SUCCESS", 결과 포함
  }

  public static <T> BaseResponse<T> fail(CustomErrorResponse customErrorResponse) {
    return new BaseResponse<>(customErrorResponse.errorCode(), customErrorResponse.message(), null);
  }

  // result만 받는 생성자. 기본 성공 상태를 설정할 때 사용됨
  public BaseResponse(T result) {
    this.errorCode = GlobalErrorCode.SUCCESS.errorCode(); // 전역 성공 코드 설정
    this.message = GlobalErrorCode.SUCCESS.message(); // 전역 성공 메시지 설정
    this.result = result;
  }
}
