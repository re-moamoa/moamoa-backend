package com.hbbhbank.moamoa.global.security.handler;

import com.hbbhbank.moamoa.global.common.JsonResponse;
import com.hbbhbank.moamoa.global.exception.GlobalErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 인가 실패 처리 핸들러 (403 Forbidden)
 */
@Slf4j
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

  // 인가 실패 시 호출되는 메서드 (예: ROLE_USER만 접근 가능한 API에 ROLE_GUEST가 접근한 경우)
  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {

    // 보안 위협 탐지나 디버깅용
    log.warn("403 Forbidden - 접근 권한 없음. URI: {}, 원인: {}", request.getRequestURI(), accessDeniedException.getMessage());

    // JsonResponse를 통해 403 Forbidden 응답을 JSON 형식으로 클라이언트에게 전달
    JsonResponse.failure(response, GlobalErrorCode.INVALID_USER);
  }
}
