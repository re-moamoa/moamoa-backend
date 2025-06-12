package com.hbbhbank.moamoa.settlement.dto.response;

public record VerifyJoinCodeResponseDto(
  Long groupId,
  String groupName,
  boolean isValid
) {}