package com.hbbhbank.moamoa.recharge.service;

import com.hbbhbank.moamoa.recharge.dto.request.RechargeRequestDto;
import com.hbbhbank.moamoa.recharge.dto.response.RechargeResponseDto;

public interface RechargeService {
  RechargeResponseDto charge(RechargeRequestDto dto);
}
