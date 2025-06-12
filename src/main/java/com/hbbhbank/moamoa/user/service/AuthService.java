package com.hbbhbank.moamoa.user.service;

import com.hbbhbank.moamoa.global.exception.BaseException;
import com.hbbhbank.moamoa.global.security.service.JwtTokenService;
import com.hbbhbank.moamoa.user.domain.ERole;
import com.hbbhbank.moamoa.user.domain.ProfileImage;
import com.hbbhbank.moamoa.user.domain.TermsAgreement;
import com.hbbhbank.moamoa.user.domain.User;
import com.hbbhbank.moamoa.user.dto.request.LoginRequestDto;
import com.hbbhbank.moamoa.user.dto.request.SignUpRequestDto;
import com.hbbhbank.moamoa.user.dto.response.LoginResponseDto;
import com.hbbhbank.moamoa.user.dto.response.SignUpResponseDto;
import com.hbbhbank.moamoa.user.exception.UserErrorCode;
import com.hbbhbank.moamoa.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenService jwtTokenService;

  @Transactional
  public SignUpResponseDto signUp(SignUpRequestDto dto) {
    dto.validate();

    validateDuplicate(dto.email(), dto.phoneNumber());

    String encodedPassword = encodePassword(dto.password());
    User user = User.create(dto, encodedPassword);

    userRepository.save(user);

    return SignUpResponseDto.from(user);
  }


  @Transactional
  public LoginResponseDto login(LoginRequestDto loginRequestDto) {
    User user = userRepository.findByEmail(loginRequestDto.email())
      .orElseThrow(() -> BaseException.type(UserErrorCode.USER_NOT_FOUND));

    if (!passwordEncoder.matches(loginRequestDto.password(), user.getPassword())) {
      throw BaseException.type(UserErrorCode.INVALID_PASSWORD);
    }

    // 핵심 책임: 사용자 인증 성공 후 토큰 발급 요청만 위임
    return jwtTokenService.issueLoginTokens(user);
  }

  // 중복 체크 메서드
  private void validateDuplicate(String email, String phoneNumber) {
    if (userRepository.existsByEmail(email)) {
      throw BaseException.type(UserErrorCode.DUPLICATE_EMAIL);
    }

    if (userRepository.existsByPhoneNumber(phoneNumber)) {
      throw BaseException.type(UserErrorCode.INVALID_PHONE);
    }
  }

  // 패스워드 인코딩 메서드
  private String encodePassword(String rawPassword) {
    return passwordEncoder.encode(rawPassword);
  }
}