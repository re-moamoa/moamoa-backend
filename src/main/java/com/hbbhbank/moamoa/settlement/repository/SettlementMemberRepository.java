package com.hbbhbank.moamoa.settlement.repository;

import com.hbbhbank.moamoa.settlement.domain.SettlementMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SettlementMemberRepository extends JpaRepository<SettlementMember, Long> {
  List<SettlementMember> findAllByUserId(Long userId);

  /**
   * 사용자가 참여한 멤버 목록을 그룹·방장·참조지갑·통화까지 fetch join으로 조회.
   * ManyToOne 연관관계만 fetch join하고, 컬렉션(members, sharePeriods)은
   * 엔티티의 @BatchSize를 통해 IN 절 배치 로딩으로 처리한다.
   */
  @Query("SELECT DISTINCT m FROM SettlementMember m "
    + "JOIN FETCH m.group g "
    + "JOIN FETCH g.host "
    + "JOIN FETCH g.referencedWallet rw "
    + "JOIN FETCH rw.currency "
    + "WHERE m.user.id = :userId")
  List<SettlementMember> findAllByUserIdWithGroup(@Param("userId") Long userId);
}
