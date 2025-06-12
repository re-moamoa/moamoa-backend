package com.hbbhbank.moamoa.settlement.service;

import com.hbbhbank.moamoa.global.exception.BaseException;
import com.hbbhbank.moamoa.settlement.domain.*;
import com.hbbhbank.moamoa.settlement.dto.request.CreateSettlementGroupRequestDto;
import com.hbbhbank.moamoa.settlement.dto.request.VerifyJoinCodeRequestDto;
import com.hbbhbank.moamoa.settlement.dto.response.*;
import com.hbbhbank.moamoa.settlement.exception.SettlementErrorCode;
import com.hbbhbank.moamoa.settlement.repository.*;
import com.hbbhbank.moamoa.user.domain.User;
import com.hbbhbank.moamoa.user.service.UserService;
import com.hbbhbank.moamoa.wallet.domain.*;
import com.hbbhbank.moamoa.wallet.dto.response.transaction.TransactionResponseDto;
import com.hbbhbank.moamoa.wallet.exception.WalletErrorCode;
import com.hbbhbank.moamoa.wallet.repository.ExternalWalletTransactionRepository;
import com.hbbhbank.moamoa.wallet.repository.InternalWalletTransactionRepository;
import com.hbbhbank.moamoa.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class SettlementGroupServiceImpl implements SettlementGroupService {

  private final SettlementGroupRepository groupRepository;
  private final SettlementTransactionRepository settlementTransactionRepository;
  private final UserService userService;
  private final WalletRepository walletRepository;
  private final InternalWalletTransactionRepository internalWalletTransactionRepository;
  private final SettlementTransactionQueryRepository settlementTransactionQueryRepository;
  private final SettlementSharePeriodRepository sharePeriodRepository;
  private final SettlementMemberRepository memberRepository;
  private final ExternalWalletTransactionRepository externalWalletTransactionRepository;

  /**
   * 정산 그룹 생성
   * 그룹 이름 입력, 공유 지갑 선택, 최대 멤버 수 입력
   * 지갑 1개 당 2개 이상의 정산 그룹을 생성할 수 없음.
   * 지갑이 반드시 존재해야함.
   */
  @Override
  @Transactional
  public CreateSettlementGroupResponseDto createGroup(CreateSettlementGroupRequestDto request) {
    // 1. 현재 로그인한 사용자 조회
    User host = userService.getCurrentUser();

    // 2. 지갑 조회
    Wallet wallet = walletRepository.findById(request.walletId())
      .orElseThrow(() -> new BaseException(SettlementErrorCode.GROUP_NOT_FOUND));

    // 3. 해당 지갑이 이미 공유 지갑으로 사용 중인지 확인 -> 이미 공유 지갑으로 사용 중이면 예외 처리
    if (groupRepository.existsByReferencedWallet(wallet)) {
      throw new BaseException(SettlementErrorCode.SETTLEMENT_ALREADY_STARTED);
    }

    // 4. 그룹 생성
    SettlementGroup group = SettlementGroup.builder()
      .groupName(request.groupName())
      .joinCode(UUID.randomUUID().toString().substring(0, 8))
      .groupStatus(GroupStatus.INACTIVE)
      .settlementStatus(SettlementStatus.BEFORE)
      .host(host)
      .referencedWallet(wallet)
      .maxMembers(request.maxMembers())
      .build();

    groupRepository.save(group);

    return new CreateSettlementGroupResponseDto(
      group.getId(), group.getGroupName(), group.getJoinCode(), request.maxMembers()
    );
  }

  /**
   * 초대 코드로 정산 그룹 방 참여
   */
  @Override
  @Transactional
  public VerifyJoinCodeResponseDto verifyJoinCode(VerifyJoinCodeRequestDto request) {
    // 1. 초대 코드로 그룹 조회
    SettlementGroup group = groupRepository.findByJoinCode(request.joinCode())
      .orElseThrow(() -> new BaseException(SettlementErrorCode.GROUP_NOT_FOUND));

    // 2. 초대 코드 시도 횟수 증가
    group.incrementJoinAttemptCount();

    // 3. 초대 코드 만료 여부 확인
    boolean valid = group.isJoinCodeValid() && group.getJoinAttemptCount() <= 5;

    // 4. 유효한 경우 → 현재 로그인한 사용자를 그룹에 멤버로 등록 (중복 방지)
    if (valid) {
      User currentUser = userService.getCurrentUser();

      boolean alreadyJoined = group.getMembers().stream()
        .anyMatch(m -> m.getUser().getId().equals(currentUser.getId()));

      if (!alreadyJoined) {
        SettlementMember newMember = SettlementMember.builder()
          .user(currentUser)
          .group(group)
          .build();
        group.getMembers().add(newMember); // 연관관계 편의 메서드로도 가능
        memberRepository.save(newMember);
      }
    }

    return new VerifyJoinCodeResponseDto(group.getId(), group.getGroupName(), valid);
  }

  /**
   * 방장 -> 초대 코드 만료 시 재발급
   */
  @Override
  @Transactional
  public ReissueJoinCodeResponseDto reissueJoinCode(Long groupId) {

    // 1. 정산 그룹 조회
    SettlementGroup group = groupRepository.findById(groupId)
      .orElseThrow(() -> new BaseException(SettlementErrorCode.GROUP_NOT_FOUND));

    // 2. 새로운 초대 코드 발급
    String newCode = UUID.randomUUID().toString().substring(0, 8);

    // 3. 초대 코드 만료 시간 설정 (10분 후)
    LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(10);

    // 4. 초대 코드 업데이트
    group.updateJoinCode(newCode, expiredAt);

    return new ReissueJoinCodeResponseDto(group.getId(), newCode, expiredAt);
  }

  /**
   * 정산 시작
   */
  @Override
  @Transactional
  public SettlementStartResponseDto startSettlement(Long groupId) {
    SettlementGroup group = groupRepository.findById(groupId)
      .orElseThrow(() -> new BaseException(SettlementErrorCode.GROUP_NOT_FOUND));

    // 1. 공유 주기 종료 전에 정산 금액 계산
    BigDecimal totalAmount = settlementTransactionQueryRepository.sumNetSettlementAmount(group);
    if (totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new BaseException(SettlementErrorCode.NO_ZERO_TO_SETTLE);
    }

    // 2. 공유 주기 종료
    group.getSharePeriods().stream()
      .filter(p -> !p.isClosed())
      .findFirst()
      .ifPresent(p -> p.stop(LocalDateTime.now()));

    // 3. 그룹 비활성화, 정산 상태 변경
    group.deactivate();
    group.markSettlementInProgress();

    // 4. 정산 멤버 상태 초기화
    group.getMembers().forEach(SettlementMember::resetTransferred);

    // 5. 정산 금액 계산
    int totalMemberCount = group.getMembers().size() + 1; // 방장 포함
    BigDecimal dividedAmount = totalAmount.divide(BigDecimal.valueOf(totalMemberCount), 2, RoundingMode.DOWN);

    return new SettlementStartResponseDto(totalMemberCount, totalAmount, dividedAmount);
  }

  /**
   * 정산 취소
   * - 송금 완료된 정산 내역이 존재하면, 환불 처리 수행
   * - 모든 정산 트랜잭션 삭제
   * - 정산 상태 초기화 및 그룹 재활성화
   * - 거래 공유 종료 시점 초기화
   */
  @Override
  @Transactional
  public void cancelSettlement(Long groupId) {
    // 1. 정산 그룹 조회
    SettlementGroup group = groupRepository.findById(groupId)
      .orElseThrow(() -> new BaseException(SettlementErrorCode.GROUP_NOT_FOUND));

    // 2. 현재 활성화된 공유 구간이 있다면 종료 처리
    group.getSharePeriods().stream()
      .filter(p -> p.getStoppedAt() == null)
      .findFirst()
      .ifPresent(p -> p.stop(LocalDateTime.now()));

    // 3. 기존 송금 내역 조회
    List<SettlementTransaction> transactions = settlementTransactionRepository.findByGroup(group);

    // 4. 송금 완료된 거래만 환불 처리
    for (SettlementTransaction tx : transactions) {
      if (tx.isTransferred()) {
        Wallet from = tx.getActualTransaction().getWallet();        // 송금자
        Wallet to = tx.getActualTransaction().getCounterWallet();   // 수신자(방장)
        BigDecimal amount = tx.getAmount();

        // 잔액 되돌리기
        to.decreaseBalance(amount);
        from.increaseBalance(amount);

        // 환불 트랜잭션 생성
        InternalWalletTransaction refundSend = InternalWalletTransaction.create(
          to, from, WalletTransactionType.SETTLEMENT_SEND, WalletTransactionStatus.SUCCESS, amount);
        InternalWalletTransaction refundReceive = InternalWalletTransaction.create(
          from, to, WalletTransactionType.SETTLEMENT_RECEIVE, WalletTransactionStatus.SUCCESS, amount);

        internalWalletTransactionRepository.save(refundSend);
        internalWalletTransactionRepository.save(refundReceive);
      }
    }

    // 5. 정산 트랜잭션 삭제
    settlementTransactionRepository.deleteAll(transactions);

    // 6. 그룹 상태 초기화
    group.markSettlementComplete();
    group.deactivate();
  }

  /**
   * 정산 내역 조회
   * - 공유 지갑에서 공유된 거래 내역의 총합을 계산
   * - 참여자 수로 나누어 1인당 정산 금액 계산
   * - 방장을 제외한 멤버별로 정산 응답 DTO 생성
   */
  @Override
  @Transactional(readOnly = true)
  public List<SettlementTransactionResponseDto> getSettlementTransactions(Long groupId) {
    // 1. 정산 그룹 조회
    SettlementGroup group = groupRepository.findById(groupId)
      .orElseThrow(() -> new BaseException(SettlementErrorCode.GROUP_NOT_FOUND));

    // 2. 해당 그룹의 공유 주기 목록 조회
    List<SettlementSharePeriod> periods = sharePeriodRepository.findAllByGroup(group);
    if (periods.isEmpty()) return List.of();

    // 3. 공유 지갑 출금 내역의 총합 계산
    BigDecimal totalAmount = settlementTransactionQueryRepository.sumOnlyExpensesByPeriods(group.getReferencedWallet(), periods);

    // 4. 참여자 수 = 멤버 수 + 방장 1명
    int totalParticipantCount = group.getMembers().size() + 1;

    if (totalParticipantCount <= 1) {
      throw new BaseException(SettlementErrorCode.NO_ZERO_TO_SETTLE);
    }

    // 5. 1인당 분담 금액 계산 (소수점 아래 버림)
    BigDecimal dividedAmount = totalAmount.divide(
      BigDecimal.valueOf(totalParticipantCount),
      2,
      RoundingMode.DOWN
    );

    // 6. 방장을 제외한 각 멤버별 정산 응답 DTO 생성
    return group.getMembers().stream()
      .filter(member -> !member.getUser().equals(group.getHost()))
      .map(member -> new SettlementTransactionResponseDto(
        member.getUser().getId(),                // fromUserId
        group.getHost().getId(),                 // toUserId
        totalAmount,                             // 정산 총합
        member.isHasTransferred(),               // 송금 여부
        totalParticipantCount,                   // 전체 인원 수
        dividedAmount                            // 각자 분담 금액
      ))
      .toList();
  }

  /**
   * 방장에게 송금하기
   */
  @Override
  @Transactional
  public boolean transferToHost(Long groupId) {
    User user = userService.getCurrentUser(); // 현재 로그인한 유저 객체 가져오기

    // 정산 그룹방 조회
    SettlementGroup group = groupRepository.findById(groupId)
      .orElseThrow(() -> new BaseException(SettlementErrorCode.GROUP_NOT_FOUND));

    Wallet toWallet = group.getReferencedWallet(); // 방장의 공유 지갑 조회
    if (toWallet == null) {
      throw new BaseException(WalletErrorCode.NOT_FOUND_WALLET);
    }

    // 로그인한 사용자 지갑 조회
    Wallet fromWallet = walletRepository.findByUserIdAndCurrency(user.getId(), toWallet.getCurrency())
      .orElseThrow(() -> new BaseException(WalletErrorCode.NOT_FOUND_WALLET));

    // 정산 상태가 진행 중이 아니면 예외 처리
    if (settlementTransactionRepository.existsByGroupAndFromUser(group, user)) {
      throw new BaseException(SettlementErrorCode.USER_ALREADY_TRANSFERRED);
    }

    BigDecimal totalAmount = settlementTransactionQueryRepository.sumNetSettlementAmount(group); // 정산 금액 계산
    int totalMemberCount = group.getMembers().size() + 1; // 정산 멤버 수

    // 정산 멤버 수가 0명이면 예외 처리
    if (totalMemberCount == 0) {
      throw new BaseException(SettlementErrorCode.NO_ZERO_TO_SETTLE);
    }

    // 정산 금액이 0원이면 예외 처리
    if (totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new BaseException(SettlementErrorCode.NO_ZERO_TO_SETTLE);
    }

    BigDecimal perAmount = totalAmount.divide(BigDecimal.valueOf(totalMemberCount), 2, RoundingMode.DOWN); // 1인당 정산 금액 계산

    // 송금자의 잔액이 부족한 경우 예외처리
    if (fromWallet.getBalance().compareTo(perAmount) < 0) {
      throw new BaseException(SettlementErrorCode.INSUFFICIENT_BALANCE);
    }

    // 1. 실제 이체 수행
    fromWallet.decreaseBalance(perAmount);
    toWallet.increaseBalance(perAmount);

    InternalWalletTransaction tx = InternalWalletTransaction.create(
      fromWallet, toWallet, WalletTransactionType.SETTLEMENT_SEND, WalletTransactionStatus.SUCCESS, perAmount);
    internalWalletTransactionRepository.save(tx);

    InternalWalletTransaction counterTx = InternalWalletTransaction.create(
      toWallet, fromWallet, WalletTransactionType.TRANSFER_IN, WalletTransactionStatus.SUCCESS, perAmount);
    internalWalletTransactionRepository.save(counterTx);

    // 2. 정산 트랜잭션 저장
    SettlementTransaction st = SettlementTransaction.create(group, user, perAmount);
    st.markTransferred(tx); // 로그인한 사용자는 송금 완료된 상태로 표시
    settlementTransactionRepository.save(st);

    group.getMembers().stream()
      .filter(m -> m.getUser().equals(user))
      .findFirst()
      .ifPresent(SettlementMember::markTransferred);

    // 3. 마지막 멤버까지 송금 완료했는지 확인
    boolean allDone = group.getMembers().stream().allMatch(SettlementMember::isHasTransferred);
    if (allDone) {
      group.markSettlementComplete();

      // 공유 주기 종료
      group.getSharePeriods().stream()
        .filter(p -> !p.isClosed())
        .findFirst()
        .ifPresent(p -> p.stop(LocalDateTime.now()));

      // 4. 동일 멤버로 새 그룹 생성
      SettlementGroup newGroup = SettlementGroup.builder()
        .groupName(group.getGroupName())
        .joinCode(UUID.randomUUID().toString().substring(0, 8))
        .groupStatus(GroupStatus.INACTIVE)
        .settlementStatus(SettlementStatus.BEFORE)
        .host(group.getHost())
        .referencedWallet(group.getReferencedWallet())
        .maxMembers(group.getMaxMembers())
        .build();
      groupRepository.save(newGroup);

      for (SettlementMember oldMember : group.getMembers()) {
        SettlementMember newMember = SettlementMember.builder()
          .user(oldMember.getUser())
          .group(newGroup)
          .build();
        memberRepository.save(newMember);
      }
    }
    return allDone;
  }

  /**
   * 그룹 폭파하기
   * - 정산 진행 중(IN_PROGRESS)인 경우에는 삭제 불가
   * - BEFORE 또는 COMPLETE 상태일 때만 삭제 가능
   * - COMPLETE 상태일 경우, 모든 멤버가 송금 완료했는지 검증
   */
  @Override
  @Transactional
  public void deleteGroup(Long groupId) {
    // 1. 그룹 조회
    SettlementGroup group = groupRepository.findById(groupId)
      .orElseThrow(() -> new BaseException(SettlementErrorCode.GROUP_NOT_FOUND));

    // 2. 정산이 진행 중이라면 삭제 불가
    if (group.getSettlementStatus() == SettlementStatus.IN_PROGRESS) {
      throw new BaseException(SettlementErrorCode.SETTLEMENT_IN_PROGRESS);
    }

    // 3. 그룹 삭제 (cascade 옵션을 통해 자식 엔티티도 삭제됨)
    settlementTransactionRepository.deleteAllByGroup(group);
    groupRepository.delete(group);
  }

  /**
   * 멤버가 그룹 나가기
   * - 정산이 진행 중(IN_PROGRESS)인 경우에는 나갈 수 없음
   */
  @Override
  @Transactional
  public void leaveGroup(Long groupId) {
    // 1. 현재 로그인한 사용자 조회
    User currentUser = userService.getCurrentUser();

    // 2. 정산 그룹 조회
    SettlementGroup group = groupRepository.findById(groupId)
      .orElseThrow(() -> new BaseException(SettlementErrorCode.GROUP_NOT_FOUND));

    // 3. 정산 상태가 진행 중이면 나갈 수 없음
    if (group.getSettlementStatus() == SettlementStatus.IN_PROGRESS) {
      throw new BaseException(SettlementErrorCode.SETTLEMENT_IN_PROGRESS);
    }

    // 4. 현재 유저가 그룹의 멤버인지 확인
    SettlementMember target = group.getMembers().stream()
      .filter(m -> m.getUser().equals(currentUser))
      .findFirst()
      .orElseThrow(() -> new BaseException(SettlementErrorCode.MEMBER_NOT_FOUND));

    // 5. 해당 멤버를 그룹에서 제거
    group.getMembers().remove(target);
  }

  /**
   * 방장의 공유 지갑 거래 내역 공유
   * - 정산 그룹에 설정된 공유 지갑의 거래 내역만 공유
   * - 그룹에 설정된 공유 기간 내 거래만 포함
   * - 그룹 참여자만 접근 가능
   */
  @Override
  @Transactional(readOnly = true)
  public List<TransactionResponseDto> getSharedTransactions(Long groupId) {
    // 1. 그룹 + 공유 지갑 확인
    SettlementGroup group = groupRepository.findById(groupId)
      .orElseThrow(() -> new BaseException(SettlementErrorCode.GROUP_NOT_FOUND));

    Wallet sharedWallet = group.getReferencedWallet();
    if (sharedWallet == null) {
      throw new BaseException(SettlementErrorCode.WALLET_NOT_LINKED);
    }

    // 2. 공유 주기 조회
    List<SettlementSharePeriod> periods = sharePeriodRepository.findAllByGroup(group);
    if (periods.isEmpty()) return List.of();

    // 3. 내부 거래 - wallet 기준 (방장의 송금/수신 내역만)
    List<InternalWalletTransaction> internalTxs =
      internalWalletTransactionRepository.findSharedOutgoingTransactions(sharedWallet, periods);

    // 4. 외부 거래 - wallet 기준만 포함 (counterWallet은 제외)
    List<ExternalWalletTransaction> externalTxs =
      externalWalletTransactionRepository.findByWalletAndPeriods(sharedWallet, periods);

    // 5. 정렬 + 변환
    return Stream.concat(
        internalTxs.stream().map(TransactionResponseDto::from),
        externalTxs.stream().map(TransactionResponseDto::from)
      )
      .sorted(Comparator.comparing(TransactionResponseDto::transactedAt).reversed())
      .toList();
  }

  /**
   * 정산 그룹 비활성시 방장의 지갑에서 정산 내역 공유 중지
   * 정산 내역에도 미포함
   */
  @Override
  @Transactional
  public void deactivateGroup(Long groupId) {
    SettlementGroup group = groupRepository.findById(groupId)
      .orElseThrow(() -> new BaseException(SettlementErrorCode.GROUP_NOT_FOUND));

    if (group.getGroupStatus() == GroupStatus.INACTIVE) {
      throw new BaseException(SettlementErrorCode.ALREADY_INACTIVE);
    }

    // 정산 공유 종료: 현재 열린 주기만 닫음 (과거 주기는 그대로 보존됨)
    group.getSharePeriods().stream()
      .filter(p -> !p.isClosed())
      .forEach(p -> p.stop(LocalDateTime.now()));

    // 그룹 상태를 비활성화로 변경 (새로운 공유 주기는 생성되지 않음)
    group.deactivate();
  }

  /**
   * 정산 그룹 활성시 방장의 지갑에서 정산 내역 공유 시작
   * 정산 내역에 포함 (중지 했을 때 내역은 공유에서 제외)
   */
  @Override
  @Transactional
  public void activateGroup(Long groupId) {
    // 1. 정산 그룹 조회
    SettlementGroup group = groupRepository.findById(groupId)
      .orElseThrow(() -> new BaseException(SettlementErrorCode.GROUP_NOT_FOUND));

    // 2. 이미 활성화된 그룹이라면 예외
    if (group.getGroupStatus() == GroupStatus.ACTIVE) {
      throw new BaseException(SettlementErrorCode.ALREADY_ACTIVE);
    }

    // 3. 그룹 활성화 및 새로운 공유 기간 시작
    group.activate();
    SettlementSharePeriod newPeriod = SettlementSharePeriod.start(group, LocalDateTime.now());
    sharePeriodRepository.save(newPeriod);
  }

  /**
   * 현재 로그인한 사용자가 방장으로 등록된 정산 그룹 목록 조회
   */
  @Override
  public List<SettlementGroupResponseDto> getMyGroups(Long userId) {
    // 방장이 나인 정산 그룹을 DB에서 조회
    return groupRepository.findByHostId(userId).stream()
      // 각 그룹 엔티티를 DTO로 변환 (isOwner: true 포함)
      .map(group -> SettlementGroupResponseDto.from(group, userId))
      .collect(Collectors.toList());
  }

  /**
   * 현재 로그인한 사용자가 참여자로 등록된 정산 그룹 목록 조회
   */
  @Override
  public List<SettlementGroupResponseDto> getJoinedGroups(Long userId) {
    return memberRepository.findAllByUserId(userId).stream()
      // SettlementMember → 그룹 가져오기
      .map(SettlementMember::getGroup)
      // null 값 제거 (예: 잘못된 member 참조 등)
      .filter(Objects::nonNull)
      // 중복 제거
      .distinct()
      // DTO 변환
      .map(group -> SettlementGroupResponseDto.from(group, userId))
      .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public SettlementGroupResponseDto getGroupDetail(Long groupId, Long userId, boolean allowIfJoinCodeValid) {
    SettlementGroup group = groupRepository.findById(groupId)
      .orElseThrow(() -> new BaseException(SettlementErrorCode.GROUP_NOT_FOUND));

    boolean isHost = group.getHost().getId().equals(userId);
    boolean isMember = group.hasMember(userId);


    if (!isHost && !isMember && !allowIfJoinCodeValid) {
      throw new BaseException(SettlementErrorCode.NO_ACCESS_TO_GROUP);
    }

    // 공유 주기 목록 변환
    List<SettlementSharePeriodDto> sharePeriods = sharePeriodRepository.findAllByGroup(group).stream()
      .map(p -> new SettlementSharePeriodDto(p.getStartedAt(), p.getStoppedAt()))
      .toList();

    return SettlementGroupResponseDto.from(group, userId);
  }

  @Override
  @Transactional(readOnly = true)
  public int getMemberCount(Long groupId) {
    SettlementGroup group = groupRepository.findById(groupId)
      .orElseThrow(() -> new BaseException(SettlementErrorCode.GROUP_NOT_FOUND));

    return group.getMembers().size();
  }

  @Override
  public List<Long> getAllMemberUserIds(Long groupId) {
    SettlementGroup group = groupRepository.findById(groupId)
      .orElseThrow(() -> new BaseException(SettlementErrorCode.GROUP_NOT_FOUND));

    List<Long> memberIds = group.getMembers().stream()
      .map(member -> member.getUser().getId())
      .collect(Collectors.toList());

    // 방장도 포함
    Long hostId = group.getHost().getId();
    if (!memberIds.contains(hostId)) {
      memberIds.add(hostId);
    }

    return memberIds;
  }
}