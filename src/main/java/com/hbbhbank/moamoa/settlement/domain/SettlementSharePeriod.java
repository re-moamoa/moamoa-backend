package com.hbbhbank.moamoa.settlement.domain;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Table(name = "settlement_share_periods")
@Getter
public class SettlementSharePeriod {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "period_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "settlement_group_id", nullable = false)
  private SettlementGroup group;

  @Column(name = "started_at", nullable = false)
  private LocalDateTime startedAt;

  @Column(name = "stopped_at")
  private LocalDateTime stoppedAt;

  public static SettlementSharePeriod start(SettlementGroup group, LocalDateTime now) {
    SettlementSharePeriod period = new SettlementSharePeriod();
    period.group = group;
    period.startedAt = now;
    return period;
  }

  public void stop(LocalDateTime now) {
    this.stoppedAt = now;
  }

  public boolean isClosed() {
    return stoppedAt != null;
  }
}
