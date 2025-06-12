package com.hbbhbank.moamoa.settlement.dto.response;

import com.hbbhbank.moamoa.settlement.domain.GroupStatus;
import com.hbbhbank.moamoa.settlement.domain.SettlementGroup;
import com.hbbhbank.moamoa.settlement.domain.SettlementMember;
import com.hbbhbank.moamoa.settlement.domain.SettlementStatus;

import java.time.LocalDateTime;
import java.util.List;

public record SettlementGroupResponseDto(
  Long id,
  String name,
  boolean isActive,
  boolean isOwner,
  int memberCount,
  int maxMembers,
  String currencyCode,
  String currencyName,
  SettlementStatus settlementStatus,
  LocalDateTime createdAt,
  List<SettlementMemberDto> members,
  HostDto host,
  List<Long> selectedMemberIds,
  boolean isSettling,
  Long myMemberId,
  List<SettlementSharePeriodDto> sharePeriods
) {
  public static SettlementGroupResponseDto from(SettlementGroup group, Long currentUserId) {
    // 현재 유저가 멤버라면, 그에 대응하는 SettlementMember의 ID 추출
    Long myMemberId = group.getMembers().stream()
      .filter(m -> m.getUser().getId().equals(currentUserId))
      .map(SettlementMember::getId)
      .findFirst()
      .orElse(null);

    // 정산 중인 경우, 참여 대상 멤버 ID 추출
    List<Long> selectedMemberIds = group.getSettlementStatus() == SettlementStatus.IN_PROGRESS
      ? group.getMembers().stream().map(SettlementMember::getId).toList()
      : null;

    // 공유 기간 목록 변환
    List<SettlementSharePeriodDto> sharePeriods = group.getSharePeriods().stream()
      .map(p -> new SettlementSharePeriodDto(p.getStartedAt(), p.getStoppedAt()))
      .toList();

    return new SettlementGroupResponseDto(
      group.getId(),
      group.getGroupName(),
      group.getGroupStatus() == GroupStatus.ACTIVE,
      group.getHost().getId().equals(currentUserId),
      group.getMembers().size(),
      group.getMaxMembers(),
      group.getReferencedWallet().getCurrency().getCode(),
      group.getReferencedWallet().getCurrency().getName(),
      group.getSettlementStatus(),
      group.getCreatedAt(),
      group.getMembers().stream().map(SettlementMemberDto::from).toList(),
      HostDto.from(group.getHost()),
      selectedMemberIds,
      group.getSettlementStatus() == SettlementStatus.IN_PROGRESS,
      myMemberId,
      sharePeriods
    );
  }
}
