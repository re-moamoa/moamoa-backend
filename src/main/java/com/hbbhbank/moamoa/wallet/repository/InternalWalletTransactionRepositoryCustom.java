package com.hbbhbank.moamoa.wallet.repository;

import com.hbbhbank.moamoa.settlement.domain.SettlementSharePeriod;
import com.hbbhbank.moamoa.wallet.domain.InternalWalletTransaction;
import com.hbbhbank.moamoa.wallet.domain.Wallet;
import com.hbbhbank.moamoa.wallet.domain.WalletTransactionType;
import com.querydsl.core.types.Predicate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface InternalWalletTransactionRepositoryCustom {
  List<InternalWalletTransaction> findAllByPredicate(Predicate predicate);

  Optional<BigDecimal> sumAmountByWalletAndTypesAndPeriods(Wallet wallet, List<WalletTransactionType> type, Predicate predicate);

  List<InternalWalletTransaction> findSharedOutgoingTransactions(Wallet sharedWallet, List<SettlementSharePeriod> periods);
}

