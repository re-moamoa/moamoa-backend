package com.hbbhbank.moamoa.settlement.dto.response;

public record CreateSettlementGroupResponseDto(
  Long groupId,
  String groupName,
  String joinCode,
  Integer maxMembers
) {}