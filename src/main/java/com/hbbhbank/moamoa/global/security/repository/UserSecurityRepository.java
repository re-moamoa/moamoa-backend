package com.hbbhbank.moamoa.global.security.repository;

import com.hbbhbank.moamoa.user.domain.User;
import com.hbbhbank.moamoa.user.projection.UserSecurityForm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * 사용자 인증 관련 Projection 데이터(UserSecurityForm)를 조회하는 Repository
 * 인증/인가 전용 최소 사용자 정보를 JPQL로 선택적으로 조회
 */
public interface UserSecurityRepository extends JpaRepository<User, Long> {

  /**
   * 이메일을 기준으로 사용자 ID와 Role만 projection 형태로 조회하는 JPQL 쿼리
   *
   * 이유: 로그인 시 전체 User 엔티티를 불러오는 것은 비효율적이며, 인증에는 최소한의 정보만 필요
   * 기능: UserSecurityForm 인터페이스를 통해 id, role만 매핑하여 반환
   * 영향: 메모리 사용 절감, 조회 성능 향상, 보안 측면에서 불필요한 데이터 노출 방지
   */
  @Query("select u.id as id, u.role as role from User u where u.email = :email")
  Optional<UserSecurityForm> findUserSecurityFromByEmail(@Param("email") String email);

  /**
   * 사용자 ID로부터 인증 정보를 projection 형태로 조회하는 JPQL 쿼리
   *
   * 이유: JWT 토큰의 userId를 기준으로 사용자 권한 정보를 불러오기 위해 사용
   * 기능: UserSecurityForm projection으로 id, role만 가져옴
   * 영향: 동일하게 성능 최적화 및 보안에 이점
   */
  @Query("select u.id as id, u.role as role from User u where u.id = :id")
  Optional<UserSecurityForm> findUserSecurityFromById(@Param("id") Long id);
}
