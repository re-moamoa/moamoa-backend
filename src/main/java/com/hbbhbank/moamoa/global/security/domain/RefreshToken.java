package com.hbbhbank.moamoa.global.security.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id; // 내부 식별용 PK (비즈니스 로직에는 사용하지 않음)

  @Column(nullable = false, unique = true) // 사용자 1명당 하나의 Refresh Token만 존재해야 하므로 unique 제약 추가
  private Long userId; // 해당 토큰이 연결된 사용자 ID (foreign key 아님, 독립적으로 관리) - 무상태 인증

  @Column(nullable = false, length = 512)
  private String token; // 토큰 문자열. 충분한 길이를 위해 512자 지정

  @Column(nullable = false)
  private LocalDateTime expiresAt; // 토큰 만료 시각

  public RefreshToken(Long userId, String token, LocalDateTime expiresAt) {
    this.userId = userId;
    this.token = token;
    this.expiresAt = expiresAt;
  }

  // 토큰이 만료되었는지 여부를 반환, 현재 시간과 만료 시각을 비교하여 판단
  public boolean isExpired() {
    return expiresAt.isBefore(LocalDateTime.now());
  }

  public RefreshToken updateToken(String newToken, LocalDateTime newExpiresAt) {
    this.token = newToken;
    this.expiresAt = newExpiresAt;
    return this;
  }
}

