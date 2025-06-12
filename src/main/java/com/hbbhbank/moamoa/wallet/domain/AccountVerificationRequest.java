package com.hbbhbank.moamoa.wallet.domain;

import com.hbbhbank.moamoa.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicUpdate
@Table(name = "account_verification_requests")
public class AccountVerificationRequest {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "transaction_id", nullable = false, unique = true)
  private String transactionId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  @Builder
  public AccountVerificationRequest(String transactionId, User user) {
    this.transactionId = transactionId;
    this.user = user;
  }

  public static AccountVerificationRequest from(String transactionId, User user) {
    return AccountVerificationRequest.builder()
      .transactionId(transactionId)
      .user(user)
      .build();
  }
}