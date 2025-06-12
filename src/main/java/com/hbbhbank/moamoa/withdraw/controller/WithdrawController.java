package com.hbbhbank.moamoa.withdraw.controller;

import com.hbbhbank.moamoa.global.common.BaseResponse;
import com.hbbhbank.moamoa.withdraw.dto.request.PointWithdrawRequestDto;
import com.hbbhbank.moamoa.withdraw.dto.response.PointWithdrawResponseDto;
import com.hbbhbank.moamoa.withdraw.service.WithdrawService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/withdraw")
public class WithdrawController {

  private final WithdrawService withdrawService;

  @PostMapping
  public ResponseEntity<BaseResponse<PointWithdrawResponseDto>> withdrawToBank(
    @RequestBody @Valid PointWithdrawRequestDto requestDto
  ) {
    return ResponseEntity.ok(BaseResponse.success(withdrawService.withdraw(requestDto)));
  }
}
