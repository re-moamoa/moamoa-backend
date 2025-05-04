package com.hbbhbank.moamoa.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder // User 생성자에서 builder 하기위해 추가
@AllArgsConstructor
@Embeddable
public class TermsAgreement {
  @Column(name="serviceTermsAgreed", nullable = false)
  private boolean serviceTermsAgreed;

  @Column(name="privacyPolicyAgreed", nullable = false)
  private boolean privacyPolicyAgreed;

  @Column(name="marketingAgreed", nullable = false)
  private boolean marketingAgreed; // 선택 사항
}

