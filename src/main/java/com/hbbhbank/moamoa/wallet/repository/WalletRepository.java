package com.hbbhbank.moamoa.wallet.repository;

import com.hbbhbank.moamoa.wallet.domain.Currency;
import com.hbbhbank.moamoa.wallet.domain.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long>, WalletRepositoryCustom {
  Optional<Wallet> findByUserId(Long userId);
  Optional<Wallet> findByUserIdAndCurrency(Long userId, Currency currencyCode);

  /**
   * DB 원자적 잔액 증가 — 단일 UPDATE 쿼리로 동시성 안전
   * 충전 시 사용: 락 없이도 balance = balance + amount가 DB 레벨에서 원자적으로 실행
   */
  @Modifying
  @Query("UPDATE Wallet w SET w.balance = w.balance + :amount WHERE w.id = :id")
  int increaseBalanceAtomically(@Param("id") Long id, @Param("amount") BigDecimal amount);

  /**
   * DB 원자적 잔액 차감 — 잔액 부족 시 affected rows = 0 반환
   * 출금 시 사용: WHERE balance >= amount 조건으로 잔액 체크와 차감을 단일 쿼리에서 처리
   */
  @Modifying
  @Query("UPDATE Wallet w SET w.balance = w.balance - :amount WHERE w.id = :id AND w.balance >= :amount")
  int decreaseBalanceAtomically(@Param("id") Long id, @Param("amount") BigDecimal amount);
}
