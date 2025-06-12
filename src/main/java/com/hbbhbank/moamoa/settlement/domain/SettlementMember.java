package com.hbbhbank.moamoa.settlement.domain;

import com.hbbhbank.moamoa.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "settlement_members")
public class SettlementMember {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "settlement_member_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "settlement_group_id", nullable = false)
  private SettlementGroup group; // 소속된 정산 그룹

  @Column(name = "has_transferred", nullable = false)
  private boolean hasTransferred = false; // 송금 여부

  @Builder
  public SettlementMember(User user, SettlementGroup group) {
    this.user = user;
    this.group = group;
  }

  public void markTransferred() {
    this.hasTransferred = true;
  }

  public void resetTransferred() {
    this.hasTransferred = false;
  }
}

