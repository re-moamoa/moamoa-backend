package com.hbbhbank.moamoa.global.security.handler;

import com.hbbhbank.moamoa.global.constant.Constants;
import com.hbbhbank.moamoa.global.exception.BaseException;
import com.hbbhbank.moamoa.global.exception.GlobalErrorCode;
import com.hbbhbank.moamoa.global.security.service.JwtTokenService;
import com.hbbhbank.moamoa.global.security.util.HeaderUtil;
import com.hbbhbank.moamoa.global.security.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component // Spring 빈으로 등록 → SecurityConfig에서 자동으로 인식됨
@RequiredArgsConstructor // final 필드 기반 생성자 주입
public class CustomLogoutProcessHandler implements LogoutHandler {

  private final JwtTokenService jwtTokenService; // RefreshToken 삭제 등의 로직 담당 서비스
  private final JwtUtil jwtUtil; // JWT 파싱 유틸

  @Override
  @Transactional // 토큰 삭제 등의 작업이 실패 없이 원자적으로 처리되도록 트랜잭션 설정
  public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

    // 인증 객체가 null이라면 로그아웃 자체를 시도할 자격이 없음 → UNAUTHORIZED 예외
    if (authentication == null) {
      throw new BaseException(GlobalErrorCode.UNAUTHORIZED);
    }

    // HTTP 요청 헤더에서 AccessToken을 추출
    // Bearer 접두사를 제거한 실제 토큰 문자열을 가져옴
    String accessToken = resolveAccessToken(request);

    // AccessToken에서 Claims(사용자 정보 등)를 추출 → 토큰이 유효한지 검증 포함
    Claims claims = parseTokenClaims(accessToken);

    // Claims에서 사용자 ID를 꺼냄 (Long 타입으로 저장되었다고 가정)
    Long userId = claims.get(Constants.CLAIM_USER_ID, Long.class);

    // 해당 사용자에 대한 RefreshToken 삭제 (Redis 등에서)
    jwtTokenService.deleteRefreshToken(userId);

    // 로그 기록 (선택) → 이후 모니터링이나 감사 로그로 사용 가능
    log.info("사용자 ID {}의 로그아웃 처리 완료", userId);
  }

  // 액세스 토큰을 HTTP 요청 헤더에서 추출하는 유틸 메서드
  private String resolveAccessToken(HttpServletRequest request) {
    return HeaderUtil.refineHeader(request, Constants.PREFIX_AUTH, Constants.PREFIX_BEARER)
      .orElseThrow(() -> new BaseException(GlobalErrorCode.INVALID_HEADER_VALUE));
  }

  // JWT 파싱 및 예외 처리 메서드
  private Claims parseTokenClaims(String token) {
    try {
      return jwtUtil.parseClaims(token);
    } catch (Exception e) {
      // 만료, 조작 등 유효하지 않은 토큰이면 예외 발생
      throw new BaseException(GlobalErrorCode.INVALID_TOKEN);
    }
  }
}
