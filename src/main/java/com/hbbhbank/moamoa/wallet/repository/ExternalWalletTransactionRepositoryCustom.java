package com.hbbhbank.moamoa.wallet.repository;

import com.hbbhbank.moamoa.settlement.domain.SettlementSharePeriod;
import com.hbbhbank.moamoa.wallet.domain.ExternalWalletTransaction;
import com.hbbhbank.moamoa.wallet.domain.Wallet;
import com.querydsl.core.types.Predicate;

import java.util.List;

public interface ExternalWalletTransactionRepositoryCustom {
  List<ExternalWalletTransaction> findAllByPredicate(Predicate predicate);

  List<ExternalWalletTransaction> findByWalletAndPeriods(Wallet wallet, List<SettlementSharePeriod> periods);
}
