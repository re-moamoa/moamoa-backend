package com.hbbhbank.moamoa.settlement.controller;

import com.hbbhbank.moamoa.global.common.BaseResponse;
import com.hbbhbank.moamoa.settlement.dto.request.CreateSettlementGroupRequestDto;
import com.hbbhbank.moamoa.settlement.dto.request.VerifyJoinCodeRequestDto;
import com.hbbhbank.moamoa.settlement.dto.response.*;
import com.hbbhbank.moamoa.settlement.service.SettlementGroupService;
import com.hbbhbank.moamoa.transfer.dto.request.PointTransferRequestDto;
import com.hbbhbank.moamoa.wallet.dto.response.transaction.TransactionResponseDto;
import com.hbbhbank.moamoa.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/settlement-groups")
@RequiredArgsConstructor
public class SettlementGroupController {

  private final SettlementGroupService settlementGroupService;
  private final UserService userService;

  /**
   * 정산 그룹 생성
   */
  @PostMapping
  public ResponseEntity<BaseResponse<CreateSettlementGroupResponseDto>> createGroup(
    @RequestBody @Valid CreateSettlementGroupRequestDto request
  ) {
    return ResponseEntity.ok(
      BaseResponse.success(settlementGroupService.createGroup(request))
    );
  }

  /**
   * 초대 코드 유효성 검증
   */
  @PostMapping("/verify-code")
  public ResponseEntity<BaseResponse<VerifyJoinCodeResponseDto>> verifyJoinCode(
    @RequestBody @Valid VerifyJoinCodeRequestDto request
  ) {
    return ResponseEntity.ok(
      BaseResponse.success(settlementGroupService.verifyJoinCode(request))
    );
  }

  /**
   * 초대 코드 재발급
   */
  @PostMapping("/{groupId}/reissue-code")
  public ResponseEntity<BaseResponse<ReissueJoinCodeResponseDto>> reissueJoinCode(@PathVariable Long groupId) {
    return ResponseEntity.ok(
      BaseResponse.success(settlementGroupService.reissueJoinCode(groupId))
    );
  }

  /**
   * 정산 시작
   */
  @PostMapping("/{groupId}/start")
  public ResponseEntity<BaseResponse<SettlementStartResponseDto>> startSettlement(@PathVariable Long groupId) {
    SettlementStartResponseDto result = settlementGroupService.startSettlement(groupId);
    return ResponseEntity.ok(BaseResponse.success(result));
  }

  /**
   * 정산 취소
   */
  @PostMapping("/{groupId}/cancel")
  public ResponseEntity<BaseResponse<Void>> cancelSettlement(@PathVariable Long groupId) {
    settlementGroupService.cancelSettlement(groupId);
    return ResponseEntity.ok(BaseResponse.success(null));
  }

  /**
   * 정산 내역 조회
   */
  @GetMapping("/{groupId}/transactions")
  public ResponseEntity<BaseResponse<List<SettlementTransactionResponseDto>>> getSettlementTransactions(@PathVariable Long groupId) {
    return ResponseEntity.ok(BaseResponse.success(settlementGroupService.getSettlementTransactions(groupId)));
  }

  /**
   * 방장에게 자동 송금
   */
  @PostMapping("/{groupId}/transfer")
  public ResponseEntity<BaseResponse<Boolean>> transferToHost(@PathVariable Long groupId) {
    boolean isAllDone = settlementGroupService.transferToHost(groupId);
    return ResponseEntity.ok(BaseResponse.success(isAllDone));
  }

  /**
   * 그룹 삭제
   */
  @PostMapping("/{groupId}")
  public ResponseEntity<BaseResponse<Void>> deleteGroup(@PathVariable Long groupId) {
    settlementGroupService.deleteGroup(groupId);
    return ResponseEntity.ok(BaseResponse.success(null));
  }

  /**
   * 방장의 거래 내역 공유 조회
   */
  @GetMapping("/{groupId}/shared-transactions")
  public ResponseEntity<BaseResponse<List<TransactionResponseDto>>> getSharedTransactions(
    @PathVariable Long groupId
  ) {
    List<TransactionResponseDto> txs = settlementGroupService.getSharedTransactions(groupId);
    return ResponseEntity.ok(BaseResponse.success(txs));
  }

  /**
   * 정산 그룹 비활성화 (공유 중지)
   */
  @PostMapping("/{groupId}/deactivate")
  public ResponseEntity<BaseResponse<Void>> deactivateGroup(@PathVariable Long groupId) {
    settlementGroupService.deactivateGroup(groupId);
    return ResponseEntity.ok(BaseResponse.success(null));
  }

  /**
   * 정산 그룹 활성화 (공유 시작)
   */
  @PostMapping("/{groupId}/activate")
  public ResponseEntity<BaseResponse<Void>> activateGroup(@PathVariable Long groupId) {
    settlementGroupService.activateGroup(groupId);
    return ResponseEntity.ok(BaseResponse.success(null));
  }

  /**
   * 현재 로그인한 사용자가 '방장'으로 등록된 정산 그룹 목록을 조회하는 API
   */
  @GetMapping("/my")
  public ResponseEntity<BaseResponse<List<SettlementGroupResponseDto>>> getMyGroups() {
    Long userId = userService.getCurrentUserId();
    List<SettlementGroupResponseDto> groups = settlementGroupService.getMyGroups(userId);
    return ResponseEntity.ok(BaseResponse.success(groups));
  }

  /**
   * 현재 로그인한 사용자가 '참여자'로 가입된 정산 그룹 목록을 조회하는 API
   */
  @GetMapping("/joined")
  public ResponseEntity<BaseResponse<List<SettlementGroupResponseDto>>> getJoinedGroups() {
    Long userId = userService.getCurrentUserId();
    List<SettlementGroupResponseDto> groups = settlementGroupService.getJoinedGroups(userId);
    return ResponseEntity.ok(BaseResponse.success(groups));
  }

  /**
   * 정산 그룹 상세 조회
   */
  @GetMapping("/{groupId}")
  public ResponseEntity<BaseResponse<SettlementGroupResponseDto>> getGroupDetail(
    @PathVariable Long groupId,
    @RequestParam(required = false, defaultValue = "false") boolean allowIfJoinCodeValid) {

    Long userId = userService.getCurrentUserId();
    return ResponseEntity.ok(BaseResponse.success(
      settlementGroupService.getGroupDetail(groupId, userId, allowIfJoinCodeValid)
    ));
  }

  /**
   * 멤버가 그룹 나가기
   */
  @PostMapping("/{groupId}/leave")
  public ResponseEntity<BaseResponse<Void>> leaveGroup(@PathVariable Long groupId) {
    settlementGroupService.leaveGroup(groupId);
    return ResponseEntity.ok(BaseResponse.success(null));
  }

  /**
   * 정산 그룹 멤버 수 조회
   */
  @GetMapping("/{groupId}/member-count")
  public ResponseEntity<BaseResponse<Integer>> getMemberCount(@PathVariable Long groupId) {
    return ResponseEntity.ok(BaseResponse.success(
      settlementGroupService.getMemberCount(groupId)
    ));
  }

  /**
   * 정산 그룹 멤버들의 userId 목록 조회 (방장 포함)
   */
  @GetMapping("/{groupId}/member-ids")
  public ResponseEntity<BaseResponse<List<Long>>> getGroupMemberUserIds(@PathVariable Long groupId) {
    List<Long> userIds = settlementGroupService.getAllMemberUserIds(groupId);
    log.info("요청 groupId: {}", groupId);
    log.info("현재 사용자 ID: {}", SecurityContextHolder.getContext().getAuthentication().getName());

    return ResponseEntity.ok(BaseResponse.success(userIds));
  }
}