package com.hbbhbank.moamoa.transfer.controller;

import com.hbbhbank.moamoa.global.common.BaseResponse;
import com.hbbhbank.moamoa.transfer.dto.request.PointTransferRequestDto;
import com.hbbhbank.moamoa.transfer.dto.response.PointTransferResponseDto;
import com.hbbhbank.moamoa.transfer.service.PointTransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/transfers")
public class PointTransferController {

  private final PointTransferService pointTransferService;

  /**
   * 사용자 포인트 송금 요청 (데드락 발생, 동시성 문제 미해결 - 데드락으로 어느정도 예방은 가능하지만 해결은 X)
   */
  @PostMapping("/points-v1")
  public ResponseEntity<BaseResponse<PointTransferResponseDto>> transferPointsV1(
    @RequestBody @Valid PointTransferRequestDto requestDto
  ) {
    PointTransferResponseDto responseDto = pointTransferService.transferByUserV1(requestDto);
    return ResponseEntity.ok(BaseResponse.success(responseDto));
  }

  /**
   * 사용자 포인트 송금 요청 (데드락 해결)
   */
  @PostMapping("/points-v2")
  public ResponseEntity<BaseResponse<PointTransferResponseDto>> transferPointsV2(
    @RequestBody @Valid PointTransferRequestDto requestDto
  ) {
    PointTransferResponseDto responseDto = pointTransferService.transferByUserV2(requestDto);
    return ResponseEntity.ok(BaseResponse.success(responseDto));
  }

  /**
   * 사용자 포인트 송금 요청 (데드락 해결 + 동시성 해결)
   */
  @PostMapping("/points-v3")
  public ResponseEntity<BaseResponse<PointTransferResponseDto>> transferPointsV3(
    @RequestBody @Valid PointTransferRequestDto requestDto
  ) {
    PointTransferResponseDto responseDto = pointTransferService.transferByUserV3(requestDto);
    return ResponseEntity.ok(BaseResponse.success(responseDto));
  }

}