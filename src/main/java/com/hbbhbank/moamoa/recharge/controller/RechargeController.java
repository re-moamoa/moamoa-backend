package com.hbbhbank.moamoa.recharge.controller;

import com.hbbhbank.moamoa.global.common.BaseResponse;
import com.hbbhbank.moamoa.recharge.dto.request.RechargeRequestDto;
import com.hbbhbank.moamoa.recharge.dto.response.RechargeResponseDto;
import com.hbbhbank.moamoa.recharge.service.RechargeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/recharge")
public class RechargeController {

  private final RechargeService rechargeService;

  @PostMapping
  public ResponseEntity<BaseResponse<RechargeResponseDto>> charge(
    @RequestBody @Valid RechargeRequestDto requestDto
  ) {
    return ResponseEntity.ok(BaseResponse.success(rechargeService.charge(requestDto)));
  }
}
