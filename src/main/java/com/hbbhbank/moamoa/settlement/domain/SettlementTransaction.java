package com.hbbhbank.moamoa.settlement.domain;

import com.hbbhbank.moamoa.user.domain.User;
import com.hbbhbank.moamoa.wallet.domain.InternalWalletTransaction;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicUpdate
@Table(name = "settlement_transactions",
  uniqueConstraints = @UniqueConstraint(
    name = "uk_settlement_transaction_group_user_round",
    columnNames = {"settlement_group_id", "from_user_id", "settlement_round"}))
public class SettlementTransaction { // 정산 결과 내역 (누가 누구에게 송금해야 하는지)

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "settlement_transaction_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "settlement_group_id", nullable = false)
  private SettlementGroup group;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "from_user_id", nullable = false)
  private User fromUser;

  @Column(name = "amount", nullable = false)
  private BigDecimal amount;

  @Column(name = "settlement_round", nullable = false)
  private int settlementRound;

  @Column(name = "transferred", nullable = false)
  private boolean transferred;

  @CreationTimestamp
  @Column(name = "requested_at", nullable = false, updatable = false)
  private LocalDateTime requestedAt;

  @Column(name = "transferred_at")
  private LocalDateTime transferredAt;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "wallet_transaction_id")
  private InternalWalletTransaction actualTransaction;

  @Builder
  public SettlementTransaction(SettlementGroup group, User fromUser, BigDecimal amount, int settlementRound, boolean transferred) {
    this.group = group;
    this.fromUser = fromUser;
    this.amount = amount;
    this.settlementRound = settlementRound;
    this.transferred = false;
  }

  public static SettlementTransaction create(SettlementGroup group, User fromUser, BigDecimal amount, int settlementRound) {
    return SettlementTransaction.builder()
      .group(group)
      .fromUser(fromUser)
      .amount(amount)
      .settlementRound(settlementRound)
      .transferred(false)
      .build();
  }

  public void markTransferred(InternalWalletTransaction tx) {
    this.transferred = true;
    this.transferredAt = LocalDateTime.now();
    this.actualTransaction = tx;
  }
}
