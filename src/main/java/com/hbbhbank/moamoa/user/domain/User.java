package com.hbbhbank.moamoa.user.domain;

import com.hbbhbank.moamoa.global.exception.BaseException;
import com.hbbhbank.moamoa.user.dto.request.SignUpRequestDto;
import com.hbbhbank.moamoa.user.exception.UserErrorCode;
import com.hbbhbank.moamoa.wallet.domain.Wallet;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicUpdate
@Table(name="users")
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "user_id")
  private Long id;

  @Column(name="user_name", nullable = false, length = 50)
  private String name; // 이름

  @Column(name="email", nullable = false, unique = true, length = 100)
  private String email; // 이메일

  @Column(name="phone_number", nullable = false, unique = true, length = 20)
  private String phoneNumber; // 휴대폰 번호

  @Column(name="password", nullable = false, length = 500)
  private String password; // 비밀번호

  @Column(name = "image", nullable = false)
  @Enumerated(EnumType.STRING)
  private ProfileImage profileImage;  // 프로필 이미지

  @Embedded
  private TermsAgreement terms; // 약관 동의 여부

  @Column(name = "role", nullable = false)
  @Enumerated(EnumType.STRING)
  private ERole role; // Spring Security에서 사용하는 역할

  @Column(name = "access_token", length = 2000)
  private String accessToken;

  @Column(name = "refresh_token", length = 2000)
  private String refreshToken;

  @Column(name = "expires_in")
  private Integer expiresIn;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Wallet> wallets = new ArrayList<>();


  @Builder
  public User(String name, String email, String phoneNumber, String password, ProfileImage profileImage, TermsAgreement terms, ERole role) {
    this.name = name;
    this.email = email;
    this.phoneNumber = phoneNumber;
    this.password = password;
    this.profileImage = profileImage;
    this.terms = terms;
    this.role = role;
  }

  public static User create(SignUpRequestDto dto, String encodedPassword) {
    return User.builder()
      .name(dto.name())
      .email(dto.email())
      .phoneNumber(dto.phoneNumber())
      .password(encodedPassword)
      .profileImage(ProfileImage.from(dto.profileImage()))
      .terms(TermsAgreement.create(dto))
      .role(ERole.USER)
      .build();
  }

  // 이름 변경
  public void changeUserName(String name) {
    this.name = name;
  }

  // 휴대폰 번호 변경
  public void changePhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  // 비밀번호 변경
  public void changePassword(String encodedPassword) {
    this.password = encodedPassword; // 이미 인코딩된 값만 받음
  }

  // 비밀번호 검증
  public void validatePassword(String rawPassword, PasswordEncoder encoder) {
    if (!encoder.matches(rawPassword, this.password)) {
      throw BaseException.type(UserErrorCode.INVALID_PASSWORD);
    }
  }

  public void addWallet(Wallet wallet) {
    this.wallets.add(wallet);
  }

  public void updateProfileImage(ProfileImage newImage) {
    this.profileImage = newImage;
  }

  public void updateTokens(String accessToken, String refreshToken, Integer expiresIn) {
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    this.expiresIn = expiresIn;
  }
}
