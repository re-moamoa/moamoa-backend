package com.hbbhbank.moamoa.settlement.repository;

import com.hbbhbank.moamoa.settlement.domain.SettlementGroup;
import com.hbbhbank.moamoa.wallet.domain.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SettlementGroupRepository extends JpaRepository<SettlementGroup, Long> {
  boolean existsByReferencedWallet(Wallet wallet);
  Optional<SettlementGroup> findByJoinCode(String joinCode);
  List<SettlementGroup> findByHostId(Long ownerId);
}
