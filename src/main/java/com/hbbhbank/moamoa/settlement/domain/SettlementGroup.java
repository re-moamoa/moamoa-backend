package com.hbbhbank.moamoa.settlement.domain;

import com.hbbhbank.moamoa.settlement.repository.SettlementTransactionQueryRepository;
import com.hbbhbank.moamoa.user.domain.User;
import com.hbbhbank.moamoa.wallet.domain.Wallet;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicUpdate
@Table(name = "settlement_groups")
public class SettlementGroup {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "settlement_group_id")
  private Long id;

  @Column(name = "group_name", nullable = false, length = 50)
  private String groupName; // 사용자가 직접 지정한 그룹명

  @Column(name = "join_code", nullable = false, length = 20)
  private String joinCode; // 초대 수락 시 필요한 식별자. 인증코드처럼 사용

  @Enumerated(EnumType.STRING)
  @Column(name = "group_status", nullable = false)
  private GroupStatus groupStatus; // ACTIVE, INACTIVE (그룹 활성화 여부)

  @Enumerated(EnumType.STRING)
  @Column(name = "settlement_status", nullable = false)
  private SettlementStatus settlementStatus; // BEFORE, IN_PROGRESS, COMPLETE (정산 상태)

  @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<SettlementMember> members = new ArrayList<>(); // 정산 참여자 목록, 그룹 기반으로 멤버들 조회 필요함.

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User host; // 정산 그룹 방장

  @ManyToOne(fetch = FetchType.LAZY) // 하나의 지갑은 여러 개의 정산 그룹에서 거래 내역 공유 대상으로 참조될 수 있음.
  @JoinColumn(name = "wallet_id", nullable = false)
  private Wallet referencedWallet; // 방장의 지갑

  @CreationTimestamp
  @Column(name = "group_created_at", nullable = false)
  private LocalDateTime createdAt; // 정산 그룹 생성

  @Column(name = "join_code_expired_at")
  private LocalDateTime joinCodeExpiredAt; // 초대 코드 만료 시점

  @Column(name = "join_code_attempt_count", nullable = false)
  private int joinAttemptCount = 0; // 초대 코드 입력 시도 횟수

  @Column(nullable = false)
  private Integer maxMembers;

  @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<SettlementSharePeriod> sharePeriods = new ArrayList<>();

  @OneToMany(mappedBy = "group", cascade = CascadeType.REMOVE, orphanRemoval = true)
  private List<SettlementTransaction> settlementTransactions;


  @Builder
  public SettlementGroup(String groupName, String joinCode, GroupStatus groupStatus, SettlementStatus settlementStatus,
                         User host, Wallet referencedWallet, Integer maxMembers) {
    this.groupName = groupName;
    this.joinCode = joinCode;
    this.groupStatus = groupStatus;
    this.settlementStatus = settlementStatus;
    this.host = host;
    this.referencedWallet = referencedWallet;
    this.maxMembers = maxMembers;
  }

  public void deactivate() {
    this.groupStatus = GroupStatus.INACTIVE;
  }

  public void activate() {
    this.groupStatus = GroupStatus.ACTIVE;
  }

  public void markSettlementInProgress() {
    this.settlementStatus = SettlementStatus.IN_PROGRESS;
  }

  public void markSettlementComplete() {
    this.settlementStatus = SettlementStatus.COMPLETE;
  }

  public void incrementJoinAttemptCount() {
    this.joinAttemptCount++;
  }

  // 초대 코드 검증
  public boolean isJoinCodeValid() {
    return this.joinCode != null &&
      this.joinCodeExpiredAt != null &&
      this.joinCodeExpiredAt.isAfter(LocalDateTime.now());
  }

  // 초대 코드 재발급
  public void updateJoinCode(String code, LocalDateTime expiredAt) {
    this.joinCode = code;
    this.joinCodeExpiredAt = expiredAt;
    this.joinAttemptCount = 0;
  }

  public boolean hasMember(Long userId) {
    return members.stream().anyMatch(m -> m.getUser().getId().equals(userId));
  }

}

