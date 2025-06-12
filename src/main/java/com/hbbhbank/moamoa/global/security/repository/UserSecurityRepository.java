package com.hbbhbank.moamoa.global.security.repository;

import com.hbbhbank.moamoa.user.domain.User;
import com.hbbhbank.moamoa.global.security.dto.UserSecurityForm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserSecurityRepository extends JpaRepository<User, Long> {

  @Query("select u.id as id, u.role as role from User u where u.email = :email")
  Optional<UserSecurityForm> findUserSecurityFromByEmail(@Param("email") String email);

  @Query("select u.id as id, u.role as role from User u where u.id = :id")
  Optional<UserSecurityForm> findUserSecurityFromById(@Param("id") Long id);
}
