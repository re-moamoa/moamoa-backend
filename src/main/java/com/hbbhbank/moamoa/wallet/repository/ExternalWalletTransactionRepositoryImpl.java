package com.hbbhbank.moamoa.wallet.repository;

import com.hbbhbank.moamoa.settlement.domain.SettlementSharePeriod;
import com.hbbhbank.moamoa.wallet.domain.ExternalWalletTransaction;
import com.hbbhbank.moamoa.wallet.domain.QExternalWalletTransaction;
import com.hbbhbank.moamoa.wallet.domain.Wallet;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class ExternalWalletTransactionRepositoryImpl implements ExternalWalletTransactionRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<ExternalWalletTransaction> findAllByPredicate(Predicate predicate) {
    return queryFactory
      .selectFrom(QExternalWalletTransaction.externalWalletTransaction)
      .where(predicate)
      .fetch();
  }

  @Override
  public List<ExternalWalletTransaction> findByWalletAndPeriods(Wallet wallet, List<SettlementSharePeriod> periods) {
    QExternalWalletTransaction tx = QExternalWalletTransaction.externalWalletTransaction;

    // 공유 주기 내 거래 시간 조건
    BooleanBuilder periodCondition = new BooleanBuilder();
    for (SettlementSharePeriod period : periods) {
      LocalDateTime end = period.getStoppedAt() != null ? period.getStoppedAt() : LocalDateTime.now();
      periodCondition.or(tx.transactedAt.between(period.getStartedAt(), end));
    }

    // 공유 지갑이 거래 주체(wallet)인 경우만 필터링
    BooleanBuilder walletCondition = new BooleanBuilder().and(tx.wallet.eq(wallet));

    return queryFactory
      .selectFrom(tx)
      .where(walletCondition.and(periodCondition))
      .orderBy(tx.transactedAt.asc())
      .fetch();
  }

}
