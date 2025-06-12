package com.hbbhbank.moamoa.user.repository;

import com.hbbhbank.moamoa.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

  boolean existsByEmail(String email); // 회원가입 시 이메일 중복 확인

  boolean existsByPhoneNumber(String phoneNumber); // 회원가입 시 번호 중복 검사

  Optional<User> findByEmail(String email); // 로그인용

}