package com.hbbhbank.moamoa.withdraw.service;

import com.hbbhbank.moamoa.withdraw.dto.request.PointWithdrawRequestDto;
import com.hbbhbank.moamoa.withdraw.dto.response.PointWithdrawResponseDto;

public interface WithdrawService {
  PointWithdrawResponseDto withdraw(PointWithdrawRequestDto requestDto);
}