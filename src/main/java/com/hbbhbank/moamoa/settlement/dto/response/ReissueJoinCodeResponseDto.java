package com.hbbhbank.moamoa.settlement.dto.response;

import java.time.LocalDateTime;

public record ReissueJoinCodeResponseDto(
  Long groupId,
  String newJoinCode,
  LocalDateTime expiredAt
) {}