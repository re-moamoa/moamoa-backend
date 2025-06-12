package com.hbbhbank.moamoa.wallet.repository;

import com.hbbhbank.moamoa.settlement.domain.SettlementSharePeriod;
import com.hbbhbank.moamoa.wallet.domain.InternalWalletTransaction;
import com.hbbhbank.moamoa.wallet.domain.QInternalWalletTransaction;
import com.hbbhbank.moamoa.wallet.domain.Wallet;
import com.hbbhbank.moamoa.wallet.domain.WalletTransactionType;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class InternalWalletTransactionRepositoryImpl implements InternalWalletTransactionRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  // 공유 지갑 거래 내역을 Predicate를 통해 조회하는 메서드
  @Override
  public List<InternalWalletTransaction> findAllByPredicate(Predicate predicate) {
    return queryFactory
      .selectFrom(QInternalWalletTransaction.internalWalletTransaction)
      .where(predicate)
      .fetch();
  }

  @Override
  public Optional<BigDecimal> sumAmountByWalletAndTypesAndPeriods(
    Wallet wallet,
    List<WalletTransactionType> types,
    Predicate predicate
  ) {
    QInternalWalletTransaction tx = QInternalWalletTransaction.internalWalletTransaction;

    BigDecimal result = queryFactory
      .select(tx.amount.sum())
      .from(tx)
      .where(
        tx.wallet.eq(wallet)
          .and(tx.type.in(types)) // 여러 타입에 대한 조건
          .and(predicate)
      )
      .fetchOne();

    return Optional.ofNullable(result);
  }

  @Override
  public List<InternalWalletTransaction> findSharedOutgoingTransactions(Wallet sharedWallet, List<SettlementSharePeriod> periods) {
    QInternalWalletTransaction tx = QInternalWalletTransaction.internalWalletTransaction;

    // 1. 기간 조건: 공유 기간 내 거래
    BooleanBuilder periodBuilder = new BooleanBuilder();
    for (SettlementSharePeriod period : periods) {
      LocalDateTime end = period.getStoppedAt() != null ? period.getStoppedAt() : LocalDateTime.now();
      periodBuilder.or(tx.transactedAt.between(period.getStartedAt(), end));
    }

    // 2. 출금 타입 조건: QR_PAYMENT, TRANSFER_OUT 등
    BooleanBuilder typeCondition = new BooleanBuilder()
      .and(tx.type.in(WalletTransactionType.values()))
      .and(tx.type.in(
        List.of(
          WalletTransactionType.QR_PAYMENT,
          WalletTransactionType.TRANSFER_OUT
        )
      ));

    // 3. 공유 지갑에서 발생한 거래
    BooleanBuilder walletCondition = new BooleanBuilder()
      .and(tx.wallet.eq(sharedWallet));

    return queryFactory
      .selectFrom(tx)
      .where(walletCondition.and(typeCondition).and(periodBuilder))
      .fetch();
  }

}
