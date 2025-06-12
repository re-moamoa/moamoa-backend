package com.hbbhbank.moamoa.settlement.repository;

import com.hbbhbank.moamoa.settlement.domain.SettlementGroup;
import com.hbbhbank.moamoa.settlement.domain.SettlementSharePeriod;
import com.hbbhbank.moamoa.wallet.domain.Wallet;

import java.math.BigDecimal;
import java.util.List;

public interface SettlementTransactionQueryRepository {

  /**
   * 공유 주기 내 출금 거래 총합 계산
   */
  BigDecimal sumOnlyExpensesByPeriods(Wallet wallet, List<SettlementSharePeriod> periods);

  /**
   * 공유 주기 내 입금 거래 총합 계산
   */
  BigDecimal sumOnlyIncomeByPeriods(Wallet wallet, List<SettlementSharePeriod> periods);

  /**
   * 직전 COMPLETE 이후 공유 주기 기반 정산 총합 계산
   * - 출금은 +, 입금은 -
   */
  BigDecimal sumNetSettlementAmount(SettlementGroup group);
}
