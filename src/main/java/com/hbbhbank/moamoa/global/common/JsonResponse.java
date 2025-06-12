package com.hbbhbank.moamoa.global.common;

import com.hbbhbank.moamoa.global.exception.ErrorCode;
import com.hbbhbank.moamoa.global.exception.GlobalErrorCode;
import jakarta.servlet.http.HttpServletResponse;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP 응답을 JSON 포맷으로 내려주는 유틸리티 클래스.
 * 필터나 예외 처리기에서 직접 HttpServletResponse를 구성할 때 사용합니다.
 */
public class JsonResponse {

  public static void success(HttpServletResponse response) throws IOException {
    write(response, GlobalErrorCode.SUCCESS);
  }

  public static void failure(HttpServletResponse response, ErrorCode errorCode) throws IOException {
    write(response, errorCode);
  }

  // 공통 처리 메서드로 묶어서 중복 코드 제거
  private static void write(HttpServletResponse response, ErrorCode code) throws IOException {
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    response.setStatus(code.status().value());

    Map<String, Object> body = new HashMap<>();
    body.put("errorCode", code.errorCode());
    body.put("message", code.message());
    body.put("result", null);

    response.getWriter().write(JSONValue.toJSONString(body));
  }
}

