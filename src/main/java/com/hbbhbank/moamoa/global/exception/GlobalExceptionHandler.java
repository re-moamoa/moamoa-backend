package com.hbbhbank.moamoa.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;

// 예외가 발생했을 때 애플리케이션이 일관된 JSON 형식으로 응답을 내려주도록 보장하고,
// 개발자 디버깅도 쉽게 해주기 위해 구현.
@Slf4j
@RestControllerAdvice // 모든 컨트롤러에서 발생한 예외를 가로챔
@RequiredArgsConstructor
public class GlobalExceptionHandler {
  /**
   * Custom Exception 전용 ExceptionHandler (@RequestBody)
   */
  @ExceptionHandler(BaseException.class)
  public ResponseEntity<CustomErrorResponse> applicationException(BaseException e) {
    ErrorCode code = e.getCode();
    logging(code);

    return ResponseEntity
      .status(code.status())
      .body(CustomErrorResponse.from(code));
  }

  /**
   * 요청 데이터 Validation 전용 ExceptionHandler (@RequestBody)
   */
  @ExceptionHandler(MethodArgumentNotValidException.class) // 	필드 메시지를 추출해서 사용자에게 보여줘야 함 (ex: "이메일은 필수입니다.")
  public ResponseEntity<CustomErrorResponse> methodArgumentNotValidException(MethodArgumentNotValidException e) {
    List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
    return convert(GlobalErrorCode.VALIDATION_ERROR, extractErrorMessage(fieldErrors));
  }

  /**
   * 요청 데이터 Validation 전용 ExceptionHandler (@ModelAttribute)
   */
  @ExceptionHandler(BindException.class) // 유효성 검증이지만 쿼리/폼 파라미터 기반일 때 따로 잡아야 함
  public ResponseEntity<CustomErrorResponse> bindException(BindException e) {
    List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
    return convert(GlobalErrorCode.VALIDATION_ERROR, extractErrorMessage(fieldErrors));
  }

  private String extractErrorMessage(List<FieldError> fieldErrors) {
    if (fieldErrors.size() == 1) {
      return fieldErrors.get(0).getDefaultMessage();
    }

    StringBuffer buffer = new StringBuffer();
    for (FieldError error : fieldErrors) {
      buffer.append(error.getDefaultMessage()).append("\n");
    }
    return buffer.toString();
  }

  /**
   * 존재하지 않는 Endpoint 전용 ExceptionHandler
   */
  @ExceptionHandler({NoHandlerFoundException.class, MethodArgumentTypeMismatchException.class})
  public ResponseEntity<CustomErrorResponse> noHandlerFoundException() {
    return convert(GlobalErrorCode.NOT_SUPPORTED_URI_ERROR);
  }

  /**
   * HTTP Request Method 오류 전용 ExceptionHandler
   * 메서드 에러 (GET → POST) 를 명확하게 알려줌
   */
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<CustomErrorResponse> httpRequestMethodNotSupportedException() {
    return convert(GlobalErrorCode.NOT_SUPPORTED_METHOD_ERROR);
  }

  /**
   * MediaType 전용 ExceptionHandler
   * Content-Type 관련 문제를 명확히 처리
   */
  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<CustomErrorResponse> httpMediaTypeNotSupportedException() {
    return convert(GlobalErrorCode.NOT_SUPPORTED_MEDIA_TYPE_ERROR);
  }

  /**
   * 내부 서버 오류 전용 ExceptionHandler
   */
  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<CustomErrorResponse> handleAnyException(RuntimeException e, HttpServletRequest request) {
    log.warn(e.getMessage());
    log.warn(request.toString());
    return convert(GlobalErrorCode.INTERNAL_SERVER_ERROR);
  }

  // 중복 제거 + 일관된 응답 포맷을 유지
  private ResponseEntity<CustomErrorResponse> convert(ErrorCode code) {
    return ResponseEntity
      .status(code.status())
      .body(CustomErrorResponse.from(code));
  }

  private ResponseEntity<CustomErrorResponse> convert(ErrorCode code, String message) {
    return ResponseEntity
      .status(code.status())
      .body(CustomErrorResponse.of(code, message));
  }

  private void logging(ErrorCode code) {
    log.warn("{} | {} | {}", code.status(), code.errorCode(), code.message());
  }
}
