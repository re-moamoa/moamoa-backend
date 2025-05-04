package com.hbbhbank.moamoa.global.security.util;

import com.hbbhbank.moamoa.global.constant.AuthConstant;
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

  /**
   * JWT를 파싱하여 Claims 반환
   */
  public Claims parseClaims(String token) {
    return Jwts.parserBuilder()
      .setSigningKey(key)
      .build()
      .parseClaimsJws(token)
      .getBody();
  }

  /**
   * JWT 유효성 검사 (서명 오류, 만료 등 예외 발생 시 false 반환)
   */
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

  /**
   * AccessToken 생성
   */
  public String generateAccessToken(Long id, ERole role) {
    return generateToken(id, role, accessExpiration);
  }

  /**
   * RefreshToken 생성
   */
  public String generateRefreshToken(Long id, ERole role) {
    return generateToken(id, role, refreshExpiration);
  }

  /**
   * 공통 토큰 생성 로직 (클레임 구성 및 서명)
   */
  private String generateToken(Long id, ERole role, long expiration) {
    Claims claims = Jwts.claims();
    claims.put(AuthConstant.CLAIM_USER_ID, id);
    if (role != null) claims.put(AuthConstant.CLAIM_USER_ROLE, role);

    Date now = new Date();
    return Jwts.builder()
      .setHeaderParam(Header.JWT_TYPE, Header.JWT_TYPE)
      .setClaims(claims)
      .setIssuedAt(now)
      .setExpiration(new Date(now.getTime() + expiration))
      .signWith(key)
      .compact();
  }

  /**
   * Access + RefreshToken 한 번에 발급
   */
  public JwtInfo generateTokens(Long id, ERole role) {
    return new JwtInfo(generateAccessToken(id, role), generateRefreshToken(id, role));
  }

  /**
   * 사용자 ID 추출
   */
  public Long extractUserId(String token) {
    return parseClaims(token).get(AuthConstant.CLAIM_USER_ID, Long.class);
  }

  /**
   * 사용자 Role 추출
   */
  public ERole extractUserRole(String token) {
    return ERole.valueOf(parseClaims(token).get(AuthConstant.CLAIM_USER_ROLE, String.class));
  }

  /**
   * 사용자 ID, Role을 포함한 정보 객체 추출
   */
  public JwtUserInfo extractUserInfo(String token) {
    Claims claims = parseClaims(token);
    Long userId = claims.get(AuthConstant.CLAIM_USER_ID, Long.class);
    String roleStr = claims.get(AuthConstant.CLAIM_USER_ROLE, String.class);

    if (userId == null || roleStr == null) {
      throw new BaseException(GlobalErrorCode.INVALID_JWT_PAYLOAD);
    }

    return new JwtUserInfo(userId, ERole.valueOf(roleStr));
  }
}
