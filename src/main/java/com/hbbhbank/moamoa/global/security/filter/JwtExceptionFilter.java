package com.hbbhbank.moamoa.global.security.filter;

import com.hbbhbank.moamoa.global.common.JsonResponse;
import com.hbbhbank.moamoa.global.constant.AuthConstant;
import com.hbbhbank.moamoa.global.exception.BaseException;
import com.hbbhbank.moamoa.global.exception.ErrorCode;
import com.hbbhbank.moamoa.global.exception.GlobalErrorCode;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component // 필터 체인에서 JWT 예외를 전담 처리하는 컴포넌트로 등록
public class JwtExceptionFilter extends OncePerRequestFilter {

  // 인증이 필요 없는 URI에 대해서는 필터 적용 제외
  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return AuthConstant.NOT_NEED_AUTH.contains(request.getRequestURI());
  }

  // JWT 인증 과정에서 발생하는 예외를 잡아 request에 설정
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
    throws ServletException, IOException {

    ErrorCode errorCode = null;

    try {
      filterChain.doFilter(request, response); // 다음 필터 실행
      return; // 정상 실행 시 조기 반환

    } catch (SecurityException e) {
      log.error("SecurityException 발생: {}", e.getMessage());
      errorCode = GlobalErrorCode.INVALID_USER;

    } catch (MalformedJwtException e) {
      log.error("MalformedJwtException 발생: {}", e.getMessage());
      errorCode = GlobalErrorCode.TOKEN_MALFORMED_ERROR;

    } catch (IllegalArgumentException e) {
      log.error("IllegalArgumentException 발생: {}", e.getMessage());
      errorCode = GlobalErrorCode.TOKEN_TYPE_ERROR;

    } catch (ExpiredJwtException e) {
      log.error("ExpiredJwtException 발생: {}", e.getMessage());
      errorCode = GlobalErrorCode.EXPIRED_TOKEN_ERROR;

    } catch (UnsupportedJwtException e) {
      log.error("UnsupportedJwtException 발생: {}", e.getMessage());
      errorCode = GlobalErrorCode.TOKEN_UNSUPPORTED_ERROR;

    } catch (JwtException e) {
      log.error("JwtException 발생: {}", e.getMessage());
      errorCode = GlobalErrorCode.TOKEN_UNKNOWN_ERROR;

    } catch (BaseException e) {
      log.error("BaseException 발생: {}", e.getMessage());
      errorCode = e.getCode();

    } catch (Exception e) {
      log.error("Exception 발생: {}", e.getMessage());
      errorCode = GlobalErrorCode.INTERNAL_SERVER_ERROR;
    }

    // 예외가 발생한 경우 request에 속성 설정 (핸들러가 이를 사용하여 응답 생성)
    request.setAttribute("exception", errorCode);

    // 예외 응답은 CustomAuthenticationEntryPointHandler에서 일괄 처리됨
    // 여기서는 응답을 내려주지 않고 필터 체인을 한 번 더 넘김
    filterChain.doFilter(request, response);
  }
}

