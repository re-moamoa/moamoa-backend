package com.hbbhbank.moamoa.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

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

  @Column(name="password", nullable = false, length = 20)
  private String password; // 비밀번호

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "profile_image_id")
  private ProfileImage profileImageUrl;  // 프로필 이미지

  @Embedded
  private TermsAgreement terms; // 약관 동의 여부

  @Column(name = "role", nullable = false)
  @Enumerated(EnumType.STRING)
  private ERole role; // Spring Security에서 사용하는 역할

  @Builder
  public User(String name, String email, String phoneNumber, String password, ProfileImage profileImageUrl, TermsAgreement terms, ERole role) {
    this.name = name;
    this.email = email;
    this.phoneNumber = phoneNumber;
    this.password = password;
    this.profileImageUrl = profileImageUrl;
    this.terms = terms;
    this.role = role;
  }

  // 이름 변경
  public void changeUserName(String name) {
    this.name = name;
  }

  // 이메일 변경
  public void changeEmail(String email) {
    this.email = email;
  }

  // 휴대폰 번호 변경
  public void changePhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  // 비밀번호 변경
  public void changePassword(String encodedPassword) {
    this.password = encodedPassword; // 이미 인코딩된 값만 받음
  }

  // 프로필 이미지 변경 - User에서 생성과 변경 흐름 담당
  public void changeProfileImage(ProfileImage newImage) {
    this.profileImageUrl = newImage;
  }
}