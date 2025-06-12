package com.hbbhbank.moamoa.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
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

/**
 * 전역 예외 처리기 (Spring MVC 환경)
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

  /**
   * 우리 서비스에서 직접 던지는 BaseException 처리
   */
  @ExceptionHandler(BaseException.class)
  public ResponseEntity<CustomErrorResponse> onBaseException(BaseException e) {
    ErrorCode code = e.getCode();
    log.warn("Business error: {} | {}", code.errorCode(), code.message());
    return ResponseEntity
      .status(code.status())
      .body(CustomErrorResponse.from(code));
  }

  /**
   * @RequestBody 바인딩/검증 에러 처리
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<CustomErrorResponse> onMethodArgumentNotValid(
    MethodArgumentNotValidException e,
    HttpServletRequest request
  ) {
    String msg = extractErrorMessage(e.getBindingResult().getFieldErrors());
    return convert(GlobalErrorCode.VALIDATION_ERROR, msg);
  }

  /**
   * @ModelAttribute 바인딩/검증 에러 처리
   */
  @ExceptionHandler(BindException.class)
  public ResponseEntity<CustomErrorResponse> onBindException(
    BindException e,
    HttpServletRequest request
  ) {
    String msg = extractErrorMessage(e.getBindingResult().getFieldErrors());
    return convert(GlobalErrorCode.VALIDATION_ERROR, msg);
  }

  /**
   * 잘못된 JSON (파싱) 에러 처리
   */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<CustomErrorResponse> onHttpMessageNotReadable(
    HttpMessageNotReadableException e,
    HttpServletRequest request
  ) {
    log.warn("Malformed JSON request: {}", e.getMessage());
    return convert(GlobalErrorCode.BAD_REQUEST, "잘못된 요청 바디입니다.");
  }

  /**
   * 존재하지 않는 URL 또는 파라미터 타입 불일치
   */
  @ExceptionHandler({NoHandlerFoundException.class, MethodArgumentTypeMismatchException.class})
  public ResponseEntity<CustomErrorResponse> onNotFoundOrTypeMismatch(
    Exception e,
    HttpServletRequest request
  ) {
    return convert(GlobalErrorCode.NOT_SUPPORTED_URI_ERROR);
  }

  /**
   * 잘못된 HTTP 메서드 (GET → POST 등)
   */
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<CustomErrorResponse> onMethodNotSupported(
    HttpRequestMethodNotSupportedException e,
    HttpServletRequest request
  ) {
    return convert(GlobalErrorCode.NOT_SUPPORTED_METHOD_ERROR);
  }

  /**
   * 잘못된 Media Type (Content-Type)
   */
  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<CustomErrorResponse> onMediaTypeNotSupported(
    HttpMediaTypeNotSupportedException e,
    HttpServletRequest request
  ) {
    return convert(GlobalErrorCode.NOT_SUPPORTED_MEDIA_TYPE_ERROR);
  }

  /**
   * 그 외 모든 예외 처리 (500 Internal Server Error)
   */
  @ExceptionHandler(Throwable.class)
  public ResponseEntity<CustomErrorResponse> onAnyException(
    Throwable e,
    HttpServletRequest request
  ) {
    log.error("Unhandled exception", e);
    return convert(GlobalErrorCode.INTERNAL_SERVER_ERROR);
  }

  // — 공통 헬퍼 메서드 — //

  private String extractErrorMessage(List<FieldError> fieldErrors) {
    if (fieldErrors.size() == 1) {
      return fieldErrors.get(0).getDefaultMessage();
    }
    StringBuilder sb = new StringBuilder();
    for (FieldError err : fieldErrors) {
      sb.append(err.getDefaultMessage()).append("; ");
    }
    return sb.toString();
  }

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
}
