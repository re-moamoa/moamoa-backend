package com.hbbhbank.moamoa.settlement.repository;

import com.hbbhbank.moamoa.settlement.domain.SettlementGroup;
import com.hbbhbank.moamoa.settlement.domain.SettlementSharePeriod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SettlementSharePeriodRepository extends JpaRepository<SettlementSharePeriod, Long> {
  // 정산 그룹에 해당하는 모든 공유 기간 조회
  List<SettlementSharePeriod> findAllByGroup(SettlementGroup group);
}
