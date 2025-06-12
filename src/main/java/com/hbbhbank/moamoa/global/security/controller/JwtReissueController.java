package com.hbbhbank.moamoa.global.security.controller;

import com.hbbhbank.moamoa.global.security.dto.request.RefreshTokenRequest;
import com.hbbhbank.moamoa.global.security.dto.response.NewAccessTokenResponse;
import com.hbbhbank.moamoa.global.security.service.JwtTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/jwt/reissue")
public class JwtReissueController {

  private final JwtTokenService jwtTokenService;

  @PostMapping
  public ResponseEntity<NewAccessTokenResponse> reissueToken(@RequestBody RefreshTokenRequest request) {
    String newAccessToken = jwtTokenService.reissueAccessToken(request.refreshToken());
    return ResponseEntity.ok(new NewAccessTokenResponse(newAccessToken));
  }
}

