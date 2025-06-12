package com.hbbhbank.moamoa.global.security.handler;

import com.hbbhbank.moamoa.global.common.JsonResponse;
import com.hbbhbank.moamoa.global.exception.ErrorCode;
import com.hbbhbank.moamoa.global.exception.GlobalErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class CustomAuthenticationEntryPointHandler implements AuthenticationEntryPoint {

  // 인증이 되지 않은 사용자가 보호된 리소스에 접근할 경우 실행되는 메서드
  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authenticationException) throws IOException {

    // JwtExceptionFilter 또는 JwtAuthenticationFilter에서 request.setAttribute("exception", ...) 로 넣어준 에러 코드 조회
    ErrorCode errorCode = (ErrorCode) request.getAttribute("exception");

    // 인증 예외 메시지를 기록해 원인 추적 가능하게 함
    log.warn("401 Unauthorized - 인증 실패. URI: {}, 원인: {}", request.getRequestURI(), authenticationException.getMessage());

    // 전달된 에러코드가 없을 경우 → 일반적인 유효성 실패로 간주하여 기본 에러코드 반환
    if (errorCode == null) {
      JsonResponse.failure(response, GlobalErrorCode.VALIDATION_ERROR); // 401 Unauthorized 응답 반환
    } else {
      // 전달된 에러코드가 있을 경우 해당 에러 코드로 응답
      JsonResponse.failure(response, errorCode);
    }
  }
}
