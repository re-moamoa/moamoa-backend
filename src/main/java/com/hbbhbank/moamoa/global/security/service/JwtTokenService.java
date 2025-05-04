package com.hbbhbank.moamoa.global.security.service;

import com.hbbhbank.moamoa.global.constant.AuthConstant;
import com.hbbhbank.moamoa.global.exception.BaseException;
import com.hbbhbank.moamoa.global.security.domain.RefreshToken;
import com.hbbhbank.moamoa.global.security.exception.AuthErrorCode;
import com.hbbhbank.moamoa.global.security.info.JwtUserInfo;
import com.hbbhbank.moamoa.global.security.repository.RefreshTokenRepository;
import com.hbbhbank.moamoa.global.security.util.JwtUtil;
import com.hbbhbank.moamoa.user.domain.ERole;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true) // 읽기 전용 트랜잭션으로 설정
@RequiredArgsConstructor
public class JwtTokenService {

  private final JwtUtil jwtUtil;
  private final RefreshTokenRepository refreshTokenRepository; // DB에 저장된 RefreshToken 관리

  /**
   * 로그인 시 Refresh Token 저장
   */
  @Transactional // 쓰기 작업이므로 readOnly=false로 오버라이딩
  public void saveRefreshToken(Long userId, String refreshToken, long expirySeconds) {
    // 현재 시각 기준으로 만료 일시 계산
    LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(expirySeconds);

    // 기존에 저장된 토큰이 있으면 삭제 (중복 저장 방지)
    refreshTokenRepository.findByUserId(userId).ifPresent(existing -> {
      refreshTokenRepository.deleteByUserId(userId);
    });

    // 새로운 토큰 저장
    refreshTokenRepository.save(new RefreshToken(userId, refreshToken, expiresAt));
  }

  /**
   * 클라이언트가 보낸 Refresh Token을 검증하고 새 Access Token 발급
   */
  public String reissueAccessToken(String refreshToken) {
    // 토큰 유효성 검증 (서명, 만료 여부 등)
    if (!jwtUtil.isValidToken(refreshToken)) {
      throw BaseException.type(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }

    // 토큰에서 사용자 ID와 권한 추출
    JwtUserInfo userInfo = jwtUtil.extractUserInfo(refreshToken);

    // DB에 저장된 RefreshToken과 일치하는지 확인
    RefreshToken stored = refreshTokenRepository.findByUserId(userInfo.userId())
      .orElseThrow(() -> BaseException.type(AuthErrorCode.INVALID_REFRESH_TOKEN));

    // 토큰 값이 다르면 위조된 것일 수 있으므로 예외 처리
    if (!stored.getToken().equals(refreshToken)) {
      throw BaseException.type(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }

    // 토큰 만료 여부 검사
    if (stored.isExpired()) {
      throw BaseException.type(AuthErrorCode.EXPIRED_TOKEN);
    }

    // 새로운 AccessToken 생성 후 반환
    return jwtUtil.generateAccessToken(userInfo.userId(), userInfo.role());
  }

  /**
   * 로그아웃 시 Refresh Token 삭제
   */
  @Transactional // DB에서 삭제 작업이므로 트랜잭션 적용
  public void deleteRefreshToken(Long userId) {
    refreshTokenRepository.deleteByUserId(userId);
  }

  /**
   * JWT에서 사용자 ID 추출
   */
  private Long extractUserId(String token) {
    Claims claims = jwtUtil.parseClaims(token);
    return claims.get(AuthConstant.CLAIM_USER_ID, Long.class);
  }

  /**
   * JWT에서 사용자 권한 정보 추출
   */
  private ERole extractUserRole(String token) {
    Claims claims = jwtUtil.parseClaims(token);
    return ERole.valueOf(claims.get(AuthConstant.CLAIM_USER_ROLE, String.class));
  }
}

