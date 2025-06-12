package com.hbbhbank.moamoa.settlement.service;

import com.hbbhbank.moamoa.settlement.dto.request.CreateSettlementGroupRequestDto;
import com.hbbhbank.moamoa.settlement.dto.request.VerifyJoinCodeRequestDto;
import com.hbbhbank.moamoa.settlement.dto.response.*;
import com.hbbhbank.moamoa.transfer.dto.request.PointTransferRequestDto;
import com.hbbhbank.moamoa.wallet.dto.response.transaction.TransactionResponseDto;

import java.util.List;

/**
 * SettlementGroupService는 정산 그룹 생성, 초대 코드 검증, 송금 처리, 정산 내역 계산 등
 * 정산 그룹과 관련된 핵심 비즈니스 로직을 담당합니다.
 */
public interface SettlementGroupService {

  /**
   * 정산 그룹 생성
   * - 그룹 이름, 공유 지갑 ID, 최대 참여 인원 수를 기반으로 그룹 생성
   * - 하나의 지갑으로 여러 정산 그룹을 만들 수 없음 (중복 방지)
   */
  CreateSettlementGroupResponseDto createGroup(CreateSettlementGroupRequestDto request);

  /**
   * 초대 코드 유효성 검증
   * - 초대 코드로 그룹을 조회하고, 만료 여부 및 시도 횟수를 확인
   */
  VerifyJoinCodeResponseDto verifyJoinCode(VerifyJoinCodeRequestDto request);

  /**
   * 초대 코드 재발급
   * - 기존 초대 코드가 만료되었을 경우, 새로운 코드를 생성하여 응답
   */
  ReissueJoinCodeResponseDto reissueJoinCode(Long groupId);

  /**
   * 정산 시작
   * - 현재 공유 중인 거래 기록을 중단하고, 그룹 상태를 정산 진행 중(IN_PROGRESS)으로 변경
   */
  SettlementStartResponseDto startSettlement(Long groupId);

  /**
   * 정산 취소
   * - 송금 내역이 있다면 환불 처리
   * - 모든 정산 기록 삭제 후 상태를 초기화하여 재활성화
   */
  void cancelSettlement(Long groupId);

  /**
   * 방장에게 송금 처리
   * - 송금자의 지갑에서 방장의 지갑으로 금액 이전
   * - 거래 내역 기록 및 정산 상태 업데이트
   */
  boolean transferToHost(Long groupId);

  /**
   * 정산 내역 조회
   * - 공유된 거래 총합과 인당 분담 금액 계산
   */
  List<SettlementTransactionResponseDto> getSettlementTransactions(Long groupId);

  /**
   * 정산 완료 후 그룹 삭제 (모든 멤버가 송금 완료 상태여야 가능)
   */
  void deleteGroup(Long groupId);

  /**
   * 정산 완료 후 멤버가 그룹에서 나가기
   */
  void leaveGroup(Long groupId);

  /**
   * 공유 지갑의 거래 내역 공유
   */
  List<TransactionResponseDto> getSharedTransactions(Long groupId);

  /**
   * 정산 그룹 활성화 (공유 시작)
   */
  void activateGroup(Long groupId);

  /**
   * 정산 그룹 비활성화 (공유 종료)
   */
  void deactivateGroup(Long groupId);

  List<SettlementGroupResponseDto> getMyGroups(Long userId);

  List<SettlementGroupResponseDto> getJoinedGroups(Long userId);

  SettlementGroupResponseDto getGroupDetail(Long groupId, Long userId, boolean allowIfJoinCodeValid);

  int getMemberCount(Long groupId);

  List<Long> getAllMemberUserIds(Long groupId);
}
