package com.hbbhbank.moamoa.user.domain;

import com.hbbhbank.moamoa.user.dto.request.SignUpRequestDto;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder // User 생성자에서 builder 하기위해 추가
@AllArgsConstructor
@Embeddable
public class TermsAgreement {
  @Column(name="service_terms_agreed", nullable = false)
  private boolean serviceTermsAgreed;

  @Column(name="privacy_policy_agreed", nullable = false)
  private boolean privacyPolicyAgreed;

  @Column(name="marketing_agreed", nullable = false)
  private boolean marketingAgreed; // 선택 사항

  public static TermsAgreement create(SignUpRequestDto dto) {
    return TermsAgreement.builder()
      .serviceTermsAgreed(Boolean.TRUE.equals(dto.serviceTermsAgreed()))
      .privacyPolicyAgreed(Boolean.TRUE.equals(dto.privacyPolicyAgreed()))
      .marketingAgreed(Boolean.TRUE.equals(dto.marketingAgreed()))
      .build();
  }
}

