package com.hbbhbank.moamoa.global.security.filter;

import com.hbbhbank.moamoa.global.constant.Constants;
import com.hbbhbank.moamoa.global.exception.BaseException;
import com.hbbhbank.moamoa.global.exception.GlobalErrorCode;
import com.hbbhbank.moamoa.global.security.exception.AuthErrorCode;
import com.hbbhbank.moamoa.global.security.info.JwtUserInfo;
import com.hbbhbank.moamoa.global.security.provider.JwtAuthenticationManager;
import com.hbbhbank.moamoa.global.security.util.HeaderUtil;
import com.hbbhbank.moamoa.global.security.util.JwtUtil;
import com.hbbhbank.moamoa.global.security.util.SecurityUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil;
  private final JwtAuthenticationManager jwtAuthenticationManager;

  // 인증이 필요 없는 URI는 필터 적용 제외
  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
    return Constants.NOT_NEED_AUTH.contains(request.getRequestURI());
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {

    // 요청 헤더에서 "Authorization" 값을 추출하고 "Bearer " 접두사 제거
    String token = HeaderUtil.refineHeader(request, Constants.PREFIX_AUTH, Constants.PREFIX_BEARER)
      .orElseThrow(() -> new BaseException(GlobalErrorCode.INVALID_HEADER_VALUE));

    // AccessToken만 인증 허용
    if (!jwtUtil.isAccessToken(token)) {
      throw new BaseException(AuthErrorCode.INVALID_ACCESS_TOKEN);
    }

    // JWT 유틸을 통해 사용자 ID, 권한 정보가 포함된 객체를 추출
    JwtUserInfo jwtUserInfo = jwtUtil.extractUserInfo(token);

    // 인증 전 인증 객체 생성 (자격 증명은 null)
    UsernamePasswordAuthenticationToken unauthToken =
      new UsernamePasswordAuthenticationToken(jwtUserInfo, null, null);

    // 실제 인증 처리 (권한 포함된 인증 객체 반환)
    UsernamePasswordAuthenticationToken authToken =
      (UsernamePasswordAuthenticationToken) jwtAuthenticationManager.authenticate(unauthToken);

    // 사용자의 IP, 세션 ID 등 요청 관련 정보 설정
    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

    // SecurityContext에 인증 정보 설정 (전역 인증 저장소)
    SecurityUtil.setAuthentication(authToken);

    log.info("인증 성공: userId={}, role={}", jwtUserInfo.userId(), jwtUserInfo.role());

    // 다음 필터로 요청 전달
    filterChain.doFilter(request, response);
  }
}