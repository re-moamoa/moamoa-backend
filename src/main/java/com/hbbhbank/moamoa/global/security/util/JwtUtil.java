package com.hbbhbank.moamoa.global.security.util;

import com.hbbhbank.moamoa.global.constant.Constants;
import com.hbbhbank.moamoa.global.exception.BaseException;
import com.hbbhbank.moamoa.global.exception.GlobalErrorCode;
import com.hbbhbank.moamoa.global.security.info.JwtInfo;
import com.hbbhbank.moamoa.global.security.info.JwtUserInfo;
import com.hbbhbank.moamoa.user.domain.ERole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {

  @Value("${jwt.secret}")
  private String secretKey;

  @Value("${jwt.access-token.expiration}")
  @Getter
  private Integer accessExpiration;

  @Value("${jwt.refresh-token.expiration}")
  @Getter
  private Integer refreshExpiration;

  private Key key;

  @PostConstruct
  public void initKey() {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    this.key = Keys.hmacShaKeyFor(keyBytes);
  }

  // JWT를 파싱하여 Claims 반환
  public Claims parseClaims(String token) {
    return Jwts.parserBuilder()
      .setSigningKey(key)
      .build()
      .parseClaimsJws(token)
      .getBody();
  }

  // JWT 유효성 검증
  public boolean isValidToken(String token) {
    try {
      Jwts.parserBuilder()
        .setSigningKey(key)
        .build()
        .parseClaimsJws(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      log.warn("JWT 검증 실패 - 원인: {}", e.getClass().getSimpleName(), e);
      return false;
    }
  }

  // AccessToken 생성
  public String generateAccessToken(Long id, ERole role) {
    return generateToken(id, role, accessExpiration, "ACCESS");
  }

  // RefreshToken 생성
  public String generateRefreshToken(Long id, ERole role) {
    return generateToken(id, role, refreshExpiration, "REFRESH");
  }

  // 공통 토큰 생성 로직
  private String generateToken(Long id, ERole role, long expiration, String tokenType) {
    Claims claims = Jwts.claims();
    claims.put(Constants.CLAIM_USER_ID, id);
    if (role != null) claims.put(Constants.CLAIM_USER_ROLE, role);
    claims.put("tokenType", tokenType);

    Date now = new Date();
    return Jwts.builder()
      .setHeaderParam(Header.JWT_TYPE, Header.JWT_TYPE)
      .setClaims(claims)
      .setIssuedAt(now)
      .setExpiration(new Date(now.getTime() + expiration))
      .signWith(key)
      .compact();
  }

  // Access + RefreshToken 한 번에 발급
  public JwtInfo generateTokens(Long id, ERole role) {
    String accessToken = generateAccessToken(id, role);
    String refreshToken = generateRefreshToken(id, role);
    return new JwtInfo(accessToken, refreshToken, refreshExpiration);
  }

  // 클레임에서 사용자 정보 추출
  public JwtUserInfo extractUserInfo(String token) {
    Claims claims = parseClaims(token);
    Long userId = claims.get(Constants.CLAIM_USER_ID, Long.class);
    String roleStr = claims.get(Constants.CLAIM_USER_ROLE, String.class);

    if (userId == null || roleStr == null) {
      throw new BaseException(GlobalErrorCode.INVALID_JWT_PAYLOAD);
    }

    return new JwtUserInfo(userId, ERole.valueOf(roleStr));
  }

  public boolean isAccessToken(String token) {
    return "ACCESS".equals(getTokenType(token));
  }

  public boolean isRefreshToken(String token) {
    return "REFRESH".equals(getTokenType(token));
  }

  private String getTokenType(String token) {
    try {
      Claims claims = parseClaims(token);
      return claims.get("tokenType", String.class);
    } catch (Exception e) {
      throw new BaseException(GlobalErrorCode.INVALID_JWT_PAYLOAD);
    }
  }
}
