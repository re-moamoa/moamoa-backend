package com.hbbhbank.moamoa.settlement.domain;

import com.hbbhbank.moamoa.global.exception.BaseException;
import com.hbbhbank.moamoa.settlement.exception.SettlementErrorCode;
import com.hbbhbank.moamoa.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * SettlementGroup 엔티티 도메인 로직 단위 테스트.
 *
 * 검증 포인트:
 * 1. 정산 상태 전이 규칙이 엔티티 내부에서 올바르게 강제되는지
 * 2. 그룹 활성화/비활성화 상태 전이 검증
 * 3. 초대 코드 유효성 검증 (만료 + 시도 횟수)
 * 4. 참여자 확인 로직 (방장 + 멤버)
 * 5. 멤버 수 제한 검증
 */
class SettlementGroupTest {

  private User host;
  private User member1;
  private User member2;

  @BeforeEach
  void setUp() throws Exception {
    host = createUserWithId(1L);
    member1 = createUserWithId(2L);
    member2 = createUserWithId(3L);
  }

  private SettlementGroup createGroup(GroupStatus groupStatus, SettlementStatus settlementStatus, int maxMembers) {
    return SettlementGroup.builder()
      .groupName("테스트 그룹")
      .joinCode("ABC12345")
      .groupStatus(groupStatus)
      .settlementStatus(settlementStatus)
      .host(host)
      .referencedWallet(null)
      .maxMembers(maxMembers)
      .build();
  }

  private void addMemberToGroup(SettlementGroup group, User user) {
    SettlementMember member = SettlementMember.builder()
      .user(user)
      .group(group)
      .build();
    group.getMembers().add(member);
  }

  private static User createUserWithId(Long id) throws Exception {
    User user = User.builder().name("user" + id).email("user" + id + "@test.com")
      .phoneNumber("010-0000-000" + id).password("password").build();
    Field idField = User.class.getDeclaredField("id");
    idField.setAccessible(true);
    idField.set(user, id);
    return user;
  }

  private void setJoinCodeExpiredAt(SettlementGroup group, LocalDateTime expiredAt) throws Exception {
    Field field = SettlementGroup.class.getDeclaredField("joinCodeExpiredAt");
    field.setAccessible(true);
    field.set(group, expiredAt);
  }

  private void setJoinAttemptCount(SettlementGroup group, int count) throws Exception {
    Field field = SettlementGroup.class.getDeclaredField("joinAttemptCount");
    field.setAccessible(true);
    field.set(group, count);
  }

  // --- 정산 상태 전이 ---

  @Nested
  @DisplayName("정산 상태 전이 (Settlement Status Transition)")
  class SettlementStatusTransition {

    @Test
    @DisplayName("BEFORE → IN_PROGRESS 전이 성공")
    void markSettlementInProgress_fromBefore_success() {
      SettlementGroup group = createGroup(GroupStatus.ACTIVE, SettlementStatus.BEFORE, 5);

      group.markSettlementInProgress();

      assertThat(group.getSettlementStatus()).isEqualTo(SettlementStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("IN_PROGRESS 상태에서 markSettlementInProgress 호출 시 예외")
    void markSettlementInProgress_fromInProgress_throwsException() {
      SettlementGroup group = createGroup(GroupStatus.ACTIVE, SettlementStatus.BEFORE, 5);
      group.markSettlementInProgress();

      assertThatThrownBy(group::markSettlementInProgress)
        .isInstanceOf(BaseException.class)
        .satisfies(e -> assertThat(((BaseException) e).getCode()).isEqualTo(SettlementErrorCode.SETTLEMENT_ALREADY_STARTED));
    }

    @Test
    @DisplayName("COMPLETE 상태에서 markSettlementInProgress 호출 시 예외")
    void markSettlementInProgress_fromComplete_throwsException() {
      SettlementGroup group = createGroup(GroupStatus.ACTIVE, SettlementStatus.BEFORE, 5);
      group.markSettlementInProgress();
      group.markSettlementComplete();

      assertThatThrownBy(group::markSettlementInProgress)
        .isInstanceOf(BaseException.class)
        .satisfies(e -> assertThat(((BaseException) e).getCode()).isEqualTo(SettlementErrorCode.SETTLEMENT_ALREADY_STARTED));
    }

    @Test
    @DisplayName("IN_PROGRESS → COMPLETE 전이 성공")
    void markSettlementComplete_fromInProgress_success() {
      SettlementGroup group = createGroup(GroupStatus.ACTIVE, SettlementStatus.BEFORE, 5);
      group.markSettlementInProgress();

      group.markSettlementComplete();

      assertThat(group.getSettlementStatus()).isEqualTo(SettlementStatus.COMPLETE);
    }

    @Test
    @DisplayName("BEFORE 상태에서 markSettlementComplete 호출 시 예외")
    void markSettlementComplete_fromBefore_throwsException() {
      SettlementGroup group = createGroup(GroupStatus.ACTIVE, SettlementStatus.BEFORE, 5);

      assertThatThrownBy(group::markSettlementComplete)
        .isInstanceOf(BaseException.class)
        .satisfies(e -> assertThat(((BaseException) e).getCode()).isEqualTo(SettlementErrorCode.SETTLEMENT_NOT_IN_PROGRESS));
    }

    @Test
    @DisplayName("IN_PROGRESS → BEFORE 전이 성공 (정산 취소)")
    void markSettlementBefore_fromInProgress_success() {
      SettlementGroup group = createGroup(GroupStatus.ACTIVE, SettlementStatus.BEFORE, 5);
      group.markSettlementInProgress();

      group.markSettlementBefore();

      assertThat(group.getSettlementStatus()).isEqualTo(SettlementStatus.BEFORE);
    }

    @Test
    @DisplayName("COMPLETE → BEFORE 전이 성공 (다음 라운드 초기화)")
    void markSettlementBefore_fromComplete_success() {
      SettlementGroup group = createGroup(GroupStatus.ACTIVE, SettlementStatus.BEFORE, 5);
      group.markSettlementInProgress();
      group.markSettlementComplete();

      group.markSettlementBefore();

      assertThat(group.getSettlementStatus()).isEqualTo(SettlementStatus.BEFORE);
    }

    @Test
    @DisplayName("이미 BEFORE 상태에서 markSettlementBefore 호출 시 예외")
    void markSettlementBefore_fromBefore_throwsException() {
      SettlementGroup group = createGroup(GroupStatus.ACTIVE, SettlementStatus.BEFORE, 5);

      assertThatThrownBy(group::markSettlementBefore)
        .isInstanceOf(BaseException.class)
        .satisfies(e -> assertThat(((BaseException) e).getCode()).isEqualTo(SettlementErrorCode.SETTLEMENT_ALREADY_STARTED));
    }
  }

  // --- 그룹 활성화/비활성화 ---

  @Nested
  @DisplayName("그룹 활성화/비활성화 (Group Status Transition)")
  class GroupStatusTransition {

    @Test
    @DisplayName("ACTIVE → INACTIVE 전이 성공")
    void deactivate_fromActive_success() {
      SettlementGroup group = createGroup(GroupStatus.ACTIVE, SettlementStatus.BEFORE, 5);

      group.deactivate();

      assertThat(group.getGroupStatus()).isEqualTo(GroupStatus.INACTIVE);
    }

    @Test
    @DisplayName("이미 INACTIVE 상태에서 deactivate 호출 시 예외")
    void deactivate_fromInactive_throwsException() {
      SettlementGroup group = createGroup(GroupStatus.INACTIVE, SettlementStatus.BEFORE, 5);

      assertThatThrownBy(group::deactivate)
        .isInstanceOf(BaseException.class)
        .satisfies(e -> assertThat(((BaseException) e).getCode()).isEqualTo(SettlementErrorCode.ALREADY_INACTIVE));
    }

    @Test
    @DisplayName("INACTIVE → ACTIVE 전이 성공")
    void activate_fromInactive_success() {
      SettlementGroup group = createGroup(GroupStatus.INACTIVE, SettlementStatus.BEFORE, 5);

      group.activate();

      assertThat(group.getGroupStatus()).isEqualTo(GroupStatus.ACTIVE);
    }

    @Test
    @DisplayName("이미 ACTIVE 상태에서 activate 호출 시 예외")
    void activate_fromActive_throwsException() {
      SettlementGroup group = createGroup(GroupStatus.ACTIVE, SettlementStatus.BEFORE, 5);

      assertThatThrownBy(group::activate)
        .isInstanceOf(BaseException.class)
        .satisfies(e -> assertThat(((BaseException) e).getCode()).isEqualTo(SettlementErrorCode.ALREADY_ACTIVE));
    }
  }

  // --- 정산 진행 중 검증 ---

  @Nested
  @DisplayName("정산 진행 중 검증 (validateNotInProgress)")
  class ValidateNotInProgress {

    @Test
    @DisplayName("BEFORE 상태에서는 예외 없음")
    void validateNotInProgress_before_noException() {
      SettlementGroup group = createGroup(GroupStatus.ACTIVE, SettlementStatus.BEFORE, 5);

      group.validateNotInProgress(); // 예외 없이 통과
    }

    @Test
    @DisplayName("IN_PROGRESS 상태에서 예외 발생")
    void validateNotInProgress_inProgress_throwsException() {
      SettlementGroup group = createGroup(GroupStatus.ACTIVE, SettlementStatus.BEFORE, 5);
      group.markSettlementInProgress();

      assertThatThrownBy(group::validateNotInProgress)
        .isInstanceOf(BaseException.class)
        .satisfies(e -> assertThat(((BaseException) e).getCode()).isEqualTo(SettlementErrorCode.SETTLEMENT_IN_PROGRESS));
    }

    @Test
    @DisplayName("COMPLETE 상태에서는 예외 없음")
    void validateNotInProgress_complete_noException() {
      SettlementGroup group = createGroup(GroupStatus.ACTIVE, SettlementStatus.BEFORE, 5);
      group.markSettlementInProgress();
      group.markSettlementComplete();

      group.validateNotInProgress(); // 예외 없이 통과
    }
  }

  // --- 초대 코드 검증 ---

  @Nested
  @DisplayName("초대 코드 유효성 검증 (isJoinCodeValid)")
  class JoinCodeValidation {

    @Test
    @DisplayName("만료 시간 이전 + 시도 횟수 이내면 유효")
    void isJoinCodeValid_validConditions_returnsTrue() throws Exception {
      SettlementGroup group = createGroup(GroupStatus.INACTIVE, SettlementStatus.BEFORE, 5);
      setJoinCodeExpiredAt(group, LocalDateTime.now().plusMinutes(10));
      setJoinAttemptCount(group, 3);

      assertThat(group.isJoinCodeValid()).isTrue();
    }

    @Test
    @DisplayName("만료 시간이 지났으면 무효")
    void isJoinCodeValid_expired_returnsFalse() throws Exception {
      SettlementGroup group = createGroup(GroupStatus.INACTIVE, SettlementStatus.BEFORE, 5);
      setJoinCodeExpiredAt(group, LocalDateTime.now().minusMinutes(1));
      setJoinAttemptCount(group, 0);

      assertThat(group.isJoinCodeValid()).isFalse();
    }

    @Test
    @DisplayName("시도 횟수가 5회 초과하면 무효")
    void isJoinCodeValid_tooManyAttempts_returnsFalse() throws Exception {
      SettlementGroup group = createGroup(GroupStatus.INACTIVE, SettlementStatus.BEFORE, 5);
      setJoinCodeExpiredAt(group, LocalDateTime.now().plusMinutes(10));
      setJoinAttemptCount(group, 6);

      assertThat(group.isJoinCodeValid()).isFalse();
    }

    @Test
    @DisplayName("시도 횟수가 정확히 5회면 유효 (경계값)")
    void isJoinCodeValid_exactlyMaxAttempts_returnsTrue() throws Exception {
      SettlementGroup group = createGroup(GroupStatus.INACTIVE, SettlementStatus.BEFORE, 5);
      setJoinCodeExpiredAt(group, LocalDateTime.now().plusMinutes(10));
      setJoinAttemptCount(group, 5);

      assertThat(group.isJoinCodeValid()).isTrue();
    }

    @Test
    @DisplayName("joinCodeExpiredAt이 null이면 무효")
    void isJoinCodeValid_nullExpiredAt_returnsFalse() {
      SettlementGroup group = createGroup(GroupStatus.INACTIVE, SettlementStatus.BEFORE, 5);
      // joinCodeExpiredAt 기본값은 null

      assertThat(group.isJoinCodeValid()).isFalse();
    }
  }

  // --- 참여자 확인 ---

  @Nested
  @DisplayName("참여자 확인 (isParticipant)")
  class ParticipantCheck {

    @Test
    @DisplayName("방장은 참여자로 인식")
    void isParticipant_host_returnsTrue() {
      SettlementGroup group = createGroup(GroupStatus.ACTIVE, SettlementStatus.BEFORE, 5);

      assertThat(group.isParticipant(host.getId())).isTrue();
    }

    @Test
    @DisplayName("멤버는 참여자로 인식")
    void isParticipant_member_returnsTrue() {
      SettlementGroup group = createGroup(GroupStatus.ACTIVE, SettlementStatus.BEFORE, 5);
      addMemberToGroup(group, member1);

      assertThat(group.isParticipant(member1.getId())).isTrue();
    }

    @Test
    @DisplayName("방장도 멤버도 아닌 사용자는 비참여자")
    void isParticipant_stranger_returnsFalse() {
      SettlementGroup group = createGroup(GroupStatus.ACTIVE, SettlementStatus.BEFORE, 5);

      assertThat(group.isParticipant(999L)).isFalse();
    }
  }

  // --- 멤버 수 제한 ---

  @Nested
  @DisplayName("멤버 수 제한 검증 (validateMemberLimit)")
  class MemberLimitValidation {

    @Test
    @DisplayName("최대 인원 미만이면 예외 없음")
    void validateMemberLimit_underLimit_noException() {
      SettlementGroup group = createGroup(GroupStatus.ACTIVE, SettlementStatus.BEFORE, 3);
      addMemberToGroup(group, member1);

      group.validateMemberLimit(); // 예외 없이 통과
    }

    @Test
    @DisplayName("최대 인원에 도달하면 예외 발생")
    void validateMemberLimit_atLimit_throwsException() {
      SettlementGroup group = createGroup(GroupStatus.ACTIVE, SettlementStatus.BEFORE, 2);
      addMemberToGroup(group, member1);
      addMemberToGroup(group, member2);

      assertThatThrownBy(group::validateMemberLimit)
        .isInstanceOf(BaseException.class)
        .satisfies(e -> assertThat(((BaseException) e).getCode()).isEqualTo(SettlementErrorCode.MEMBER_LIMIT_EXCEEDED));
    }
  }
}
