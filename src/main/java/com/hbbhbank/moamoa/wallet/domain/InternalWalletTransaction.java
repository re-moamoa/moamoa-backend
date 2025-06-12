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
@Table(name = "internal_wallet_transactions")
public class InternalWalletTransaction {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "wallet_transaction_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "wallet_id", nullable = false)
  private Wallet wallet; // 주체 지갑

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "counter_wallet_id")
  private Wallet counterWallet; // 상대 지갑 (송금/결제 시)

  @Enumerated(EnumType.STRING)
  @Column(name = "transaction_type", nullable = false)
  private WalletTransactionType type; // 거래 유형

  @Enumerated(EnumType.STRING)
  @Column(name = "transaction_status", nullable = false)
  private WalletTransactionStatus status; // PENDING, SUCCESS, FAILED

  @Column(name = "transaction_amount", nullable = false)
  private BigDecimal amount; // 금액 (양수/음수로 입출금 표현)

  @Column(name = "transacted_at", nullable = false, updatable = false)
  private LocalDateTime transactedAt; // 거래 일시

  @PrePersist
  public void prePersist() {
    this.transactedAt = LocalDateTime.now();
  }

  @Builder
  public InternalWalletTransaction(Wallet wallet, Wallet counterWallet, WalletTransactionType type, WalletTransactionStatus status, BigDecimal amount) {
    this.wallet = wallet;
    this.counterWallet = counterWallet;
    this.type = type;
    this.status = status;
    this.amount = amount;
  }

  public static InternalWalletTransaction create(
    Wallet wallet,
    Wallet counterWallet,
    WalletTransactionType type,
    WalletTransactionStatus status,
    BigDecimal amount
  ) {
    return InternalWalletTransaction.builder()
      .wallet(wallet)
      .counterWallet(counterWallet)
      .type(type)
      .status(status)
      .amount(amount)
      .build();
  }
}
