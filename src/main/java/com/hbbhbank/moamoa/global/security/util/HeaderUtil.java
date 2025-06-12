package com.hbbhbank.moamoa.global.security.util;

import com.hbbhbank.moamoa.global.exception.BaseException;
import com.hbbhbank.moamoa.global.exception.GlobalErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

import java.util.Optional;

public class HeaderUtil {

  // 클라이언트 요청의 헤더에서 토큰 값을 추출하고, 형식을 검증한 후 접두어(Bearer 등)를 제거하여 반환
  public static Optional<String> refineHeader(HttpServletRequest request, String headerName, String prefix) {

    String headerValue = request.getHeader(headerName);

    // 헤더가 비어 있거나 접두어로 시작하지 않으면 예외 발생 (잘못된 요청 차단 목적)
    if (!StringUtils.hasText(headerValue) || !headerValue.startsWith(prefix))
      throw new BaseException(GlobalErrorCode.INVALID_HEADER_VALUE);

    // 접두어를 제거하고 순수 토큰만 잘라서 Optional로 감싸 반환
    return Optional.of(headerValue.substring(prefix.length()));
  }
}

