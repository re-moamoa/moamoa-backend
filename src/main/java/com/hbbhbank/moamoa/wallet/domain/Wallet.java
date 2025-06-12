package com.hbbhbank.moamoa.wallet.domain;

import com.hbbhbank.moamoa.global.exception.BaseException;
import com.hbbhbank.moamoa.user.domain.User;
import com.hbbhbank.moamoa.wallet.exception.WalletErrorCode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

import java.math.BigDecimal;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicUpdate
@Table(name = "wallets", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "currency_code"}))
public class Wallet {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "wallet_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id")
  private User user; // 지갑 소유자

  @Column(name = "wallet_number", nullable = false, length = 30, updatable = false)
  private String walletNumber; // 지갑 번호 (내부용)

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "currency_code")
  private Currency currency; // 어떤 통화의 지갑인지

  @Column(name = "balance", nullable = false)
  private BigDecimal balance = BigDecimal.ZERO; // 잔액 (포인트 단위)

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "hwanbee_account_link_id", unique = true)
  private HwanbeeAccountLink hwanbeeAccount; // 환비 API 연결 계좌

  @Builder
  public Wallet(User user, String walletNumber, Currency currency, BigDecimal balance, HwanbeeAccountLink hwanbeeAccount) {
    this.user = user;
    this.walletNumber = walletNumber;
    this.currency = currency;
    this.balance = balance != null ? balance : BigDecimal.ZERO;
    this.hwanbeeAccount = hwanbeeAccount;
  }

  public static Wallet create(
    User user,
    String walletNumber,
    Currency currency,
    HwanbeeAccountLink hwanbeeAccount
  ) {
    return Wallet.builder()
      .user(user)
      .walletNumber(walletNumber)
      .currency(currency)
      .balance(BigDecimal.ZERO) // 초기값 명시
      .hwanbeeAccount(hwanbeeAccount)
      .build();
  }

  // 포인트 충전
  public void increaseBalance(BigDecimal amount) {
    this.balance = this.balance.add(amount);
  }

  // 포인트 차감 (잔액 부족 시 예외)
  public void decreaseBalance(BigDecimal amount) {
    if (this.balance.compareTo(amount) < 0) {
      throw BaseException.type(WalletErrorCode.INSUFFICIENT_BALANCE);
    }
    this.balance = this.balance.subtract(amount);
  }
}