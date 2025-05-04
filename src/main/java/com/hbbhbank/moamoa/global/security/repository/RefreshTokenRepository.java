package com.hbbhbank.moamoa.global.security.repository;

import com.hbbhbank.moamoa.global.security.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

  /**
   * 사용자 ID로 Refresh Token을 조회하는 메서드
   * - RefreshToken은 사용자별로 하나만 존재하므로 Optional로 반환
   * - AccessToken 재발급 시 사용자 ID로 토큰 존재 여부 확인에 사용됨
   */
  Optional<RefreshToken> findByUserId(Long userId);

  /**
   * 사용자 ID를 기준으로 Refresh Token을 삭제하는 메서드
   * - 로그아웃 시 RefreshToken을 삭제하여 재사용을 방지
   */
  void deleteByUserId(Long userId);
}
