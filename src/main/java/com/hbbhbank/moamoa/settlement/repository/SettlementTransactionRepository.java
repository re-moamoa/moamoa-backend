package com.hbbhbank.moamoa.settlement.repository;

import com.hbbhbank.moamoa.settlement.domain.SettlementGroup;
import com.hbbhbank.moamoa.settlement.domain.SettlementTransaction;
import com.hbbhbank.moamoa.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SettlementTransactionRepository extends JpaRepository<SettlementTransaction, Long> {

  // 정산 그룹 기준 조회
  List<SettlementTransaction> findByGroup(SettlementGroup group);

  // 특정 라운드의 정산 트랜잭션 조회
  List<SettlementTransaction> findByGroupAndSettlementRound(SettlementGroup group, int settlementRound);

  // 특정 멤버의 특정 라운드 정산 트랜잭션 조회 (startSettlement에서 미리 생성된 트랜잭션 참조)
  Optional<SettlementTransaction> findByGroupAndFromUserAndSettlementRound(
    SettlementGroup group, User fromUser, int settlementRound);

  // 중복 송금 방지용 (라운드 기반)
  boolean existsByGroupAndFromUserAndSettlementRound(SettlementGroup group, User fromUser, int settlementRound);

  void deleteAllByGroup(SettlementGroup group);
}
