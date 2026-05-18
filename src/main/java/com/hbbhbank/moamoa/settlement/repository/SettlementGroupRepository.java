package com.hbbhbank.moamoa.settlement.repository;

import com.hbbhbank.moamoa.settlement.domain.SettlementGroup;
import com.hbbhbank.moamoa.settlement.domain.SettlementStatus;
import com.hbbhbank.moamoa.wallet.domain.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SettlementGroupRepository extends JpaRepository<SettlementGroup, Long> {
  boolean existsByReferencedWalletAndSettlementStatusNot(Wallet wallet, SettlementStatus status);
  Optional<SettlementGroup> findByJoinCode(String joinCode);
  List<SettlementGroup> findByHostId(Long ownerId);

  /**
   * 방장 ID로 정산 그룹을 조회하면서 방장·참조지갑·통화를 fetch join으로 즉시 로딩.
   * 컬렉션(members, sharePeriods)은 엔티티의 @BatchSize를 통해 배치 로딩으로 처리한다.
   */
  @Query("SELECT DISTINCT g FROM SettlementGroup g "
    + "JOIN FETCH g.host "
    + "JOIN FETCH g.referencedWallet rw "
    + "JOIN FETCH rw.currency "
    + "WHERE g.host.id = :userId")
  List<SettlementGroup> findByHostIdWithDetails(@Param("userId") Long userId);
}
