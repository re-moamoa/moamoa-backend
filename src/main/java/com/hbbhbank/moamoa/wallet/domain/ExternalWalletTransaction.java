package com.hbbhbank.moamoa.wallet.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicUpdate
@Table(name = "external_wallet_transactions")
public class ExternalWalletTransaction {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "external_wallet_transaction_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "wallet_id", nullable = false)
  private Wallet wallet; // 주체 지갑

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "hwanbee_account_link_id", nullable = false)
  private HwanbeeAccountLink hwanbeeAccount; // 사용자의 환비 계좌 정보

  @Enumerated(EnumType.STRING)
  @Column(name = "transaction_type", nullable = false)
  private WalletTransactionType type; // 거래 유형

  @Enumerated(EnumType.STRING)
  @Column(name = "transaction_status", nullable = false)
  private WalletTransactionStatus status; // PENDING, SUCCESS, FAILED

  @Column(name = "transaction_amount", nullable = false)
  private BigDecimal amount; // 금액

  @Column(name = "transacted_at", nullable = false, updatable = false)
  private LocalDateTime transactedAt; // 거래 일시

  @PrePersist // 자동 생성
  public void prePersist() {
    this.transactedAt = LocalDateTime.now();
  }

  @Builder
  public ExternalWalletTransaction(Wallet wallet, HwanbeeAccountLink hwanbeeAccount, WalletTransactionType type, WalletTransactionStatus status, BigDecimal amount) {
    this.wallet = wallet;
    this.hwanbeeAccount = hwanbeeAccount;
    this.type = type;
    this.status = status;
    this.amount = amount;
  }

  public static ExternalWalletTransaction create(
    Wallet wallet,
    HwanbeeAccountLink hwanbeeAccount,
    WalletTransactionType type,
    WalletTransactionStatus status,
    BigDecimal amount
  ) {
    return ExternalWalletTransaction.builder()
      .wallet(wallet)
      .hwanbeeAccount(hwanbeeAccount)
      .type(type)
      .status(status)
      .amount(amount)
      .build();
  }
}
