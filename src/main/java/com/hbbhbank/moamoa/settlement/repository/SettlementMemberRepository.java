package com.hbbhbank.moamoa.settlement.repository;

import com.hbbhbank.moamoa.settlement.domain.SettlementMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SettlementMemberRepository extends JpaRepository<SettlementMember, Long> {
  List<SettlementMember> findAllByUserId(Long userId);
}
