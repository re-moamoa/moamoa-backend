package com.hbbhbank.moamoa.global.security.handler;

import com.hbbhbank.moamoa.global.common.JsonResponse;
import com.hbbhbank.moamoa.global.security.exception.AuthErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component // Spring 빈으로 등록됨 → SecurityConfig에 자동 적용 가능
public class CustomLogoutResultHandler implements LogoutSuccessHandler {

  /**
   * 로그아웃 성공 후 실행되는 메서드.
   * 내부적으로 로그아웃 처리는 LogoutHandler에서 이미 수행되었으며,
   * 여기서는 그 결과를 클라이언트에게 응답하는 역할만 담당.
   */
  @Override
  public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

    // 인증 정보가 없는 경우 (예: 이미 로그아웃된 상태로 로그아웃 요청한 경우)
    if (authentication == null) {
      log.info("인증 정보가 존재하지 않습니다. authentication is null.");

      // 실패 응답 반환 (예: 사용자 정보를 찾을 수 없음)
      JsonResponse.failure(response, AuthErrorCode.USER_NOT_FOUND);
      return; // 실패 후 성공 응답이 이어지지 않도록 return 처리 필수
    }

    // 인증이 정상적으로 된 사용자라면 성공 응답 반환
    JsonResponse.success(response);
  }
}

