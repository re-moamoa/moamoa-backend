package com.hbbhbank.moamoa.settlement.repository;

import com.hbbhbank.moamoa.settlement.domain.SettlementGroup;
import com.hbbhbank.moamoa.settlement.domain.SettlementTransaction;
import com.hbbhbank.moamoa.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SettlementTransactionRepository extends JpaRepository<SettlementTransaction, Long> {

  // 정산 그룹 기준 조회
  List<SettlementTransaction> findByGroup(SettlementGroup group);

  // 중복 송금 방지용
  boolean existsByGroupAndFromUser(SettlementGroup group, User fromUser);

  void deleteAllByGroup(SettlementGroup group);
}
