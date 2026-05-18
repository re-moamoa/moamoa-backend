package com.hbbhbank.moamoa.settlement.util;

import com.hbbhbank.moamoa.settlement.util.SettlementAmountDistributor.DistributionResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SettlementAmountDistributor 단위 테스트.
 *
 * 핵심 검증 포인트:
 * 1. 분배 결과 합계가 항상 totalAmount와 정확히 일치하는지 (누락 없음)
 * 2. 나머지가 0인 경우 추가 부담자가 없는지
 * 3. 나머지가 발생할 때 정확한 수의 추가 부담자가 선정되는지
 * 4. 다중 통화(KRW, USD) 환경에서 최소 단위가 올바르게 적용되는지
 */
class SettlementAmountDistributorTest {

  @Nested
  @DisplayName("KRW (최소 단위: 1원)")
  class KrwTests {

    private static final BigDecimal SMALLEST_UNIT = BigDecimal.ONE;

    @Test
    @DisplayName("나눠떨어지는 경우: 1000원 / 2명 → 각 500원, 나머지 0원")
    void distribute_evenlyDivisible() {
      // given
      BigDecimal totalAmount = new BigDecimal("1000");
      List<Long> memberIds = List.of(1L, 2L);
      Random random = new Random(42);

      // when
      DistributionResult result = SettlementAmountDistributor.distribute(
        totalAmount, memberIds, SMALLEST_UNIT, random
      );

      // then
      assertThat(result.baseAmount()).isEqualByComparingTo("500");
      assertThat(result.remainder()).isEqualByComparingTo("0");
      assertThat(result.extraPayerIds()).isEmpty();

      // 합계 검증
      BigDecimal sum = result.memberAmounts().values().stream()
        .reduce(BigDecimal.ZERO, BigDecimal::add);
      assertThat(sum).isEqualByComparingTo(totalAmount);

      // 모든 멤버 동일 금액
      assertThat(result.memberAmounts().get(1L)).isEqualByComparingTo("500");
      assertThat(result.memberAmounts().get(2L)).isEqualByComparingTo("500");
    }

    @Test
    @DisplayName("나머지 1원 발생: 1000원 / 3명 → 333원 x 2 + 334원 x 1 = 1000원")
    void distribute_remainder1() {
      // given
      BigDecimal totalAmount = new BigDecimal("1000");
      List<Long> memberIds = List.of(1L, 2L, 3L);
      Random random = new Random(42);

      // when
      DistributionResult result = SettlementAmountDistributor.distribute(
        totalAmount, memberIds, SMALLEST_UNIT, random
      );

      // then
      assertThat(result.baseAmount()).isEqualByComparingTo("333");
      assertThat(result.remainder()).isEqualByComparingTo("1");
      assertThat(result.extraPayerIds()).hasSize(1);

      // 합계 검증: 반드시 1000원과 일치
      BigDecimal sum = result.memberAmounts().values().stream()
        .reduce(BigDecimal.ZERO, BigDecimal::add);
      assertThat(sum).isEqualByComparingTo(totalAmount);

      // 당첨자는 334원, 나머지는 333원
      Long extraPayer = result.extraPayerIds().get(0);
      assertThat(result.memberAmounts().get(extraPayer)).isEqualByComparingTo("334");

      long countBase = result.memberAmounts().values().stream()
        .filter(v -> v.compareTo(new BigDecimal("333")) == 0)
        .count();
      assertThat(countBase).isEqualTo(2);
    }

    @Test
    @DisplayName("나머지 여러 원 발생: 1000원 / 7명 → 나머지 6원, 6명에게 1원씩 추가")
    void distribute_multipleRemainder() {
      // given
      BigDecimal totalAmount = new BigDecimal("1000");
      List<Long> memberIds = List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L);
      Random random = new Random(42);

      // when
      DistributionResult result = SettlementAmountDistributor.distribute(
        totalAmount, memberIds, SMALLEST_UNIT, random
      );

      // then: 1000 / 7 = 142 (버림), 나머지 = 1000 - 142*7 = 1000 - 994 = 6
      assertThat(result.baseAmount()).isEqualByComparingTo("142");
      assertThat(result.remainder()).isEqualByComparingTo("6");
      assertThat(result.extraPayerIds()).hasSize(6);

      // 합계 검증
      BigDecimal sum = result.memberAmounts().values().stream()
        .reduce(BigDecimal.ZERO, BigDecimal::add);
      assertThat(sum).isEqualByComparingTo(totalAmount);

      // 6명은 143원, 1명은 142원
      long count143 = result.memberAmounts().values().stream()
        .filter(v -> v.compareTo(new BigDecimal("143")) == 0)
        .count();
      long count142 = result.memberAmounts().values().stream()
        .filter(v -> v.compareTo(new BigDecimal("142")) == 0)
        .count();
      assertThat(count143).isEqualTo(6);
      assertThat(count142).isEqualTo(1);
    }
  }

  @Nested
  @DisplayName("USD (최소 단위: 0.01달러)")
  class UsdTests {

    private static final BigDecimal SMALLEST_UNIT = new BigDecimal("0.01");

    @Test
    @DisplayName("나머지 발생: $10.00 / 3명 → $3.33 x 2 + $3.34 x 1 = $10.00")
    void distribute_usdRemainder() {
      // given
      BigDecimal totalAmount = new BigDecimal("10.00");
      List<Long> memberIds = List.of(1L, 2L, 3L);
      Random random = new Random(42);

      // when
      DistributionResult result = SettlementAmountDistributor.distribute(
        totalAmount, memberIds, SMALLEST_UNIT, random
      );

      // then
      assertThat(result.baseAmount()).isEqualByComparingTo("3.33");
      assertThat(result.remainder()).isEqualByComparingTo("0.01");
      assertThat(result.extraPayerIds()).hasSize(1);

      // 합계 검증
      BigDecimal sum = result.memberAmounts().values().stream()
        .reduce(BigDecimal.ZERO, BigDecimal::add);
      assertThat(sum).isEqualByComparingTo(totalAmount);

      // 당첨자는 3.34, 나머지는 3.33
      Long extraPayer = result.extraPayerIds().get(0);
      assertThat(result.memberAmounts().get(extraPayer)).isEqualByComparingTo("3.34");
    }

    @Test
    @DisplayName("나눠떨어지는 경우: $10.00 / 4명 → 각 $2.50")
    void distribute_usdEvenlyDivisible() {
      // given
      BigDecimal totalAmount = new BigDecimal("10.00");
      List<Long> memberIds = List.of(1L, 2L, 3L, 4L);
      Random random = new Random(42);

      // when
      DistributionResult result = SettlementAmountDistributor.distribute(
        totalAmount, memberIds, SMALLEST_UNIT, random
      );

      // then
      assertThat(result.baseAmount()).isEqualByComparingTo("2.50");
      assertThat(result.remainder()).isEqualByComparingTo("0");
      assertThat(result.extraPayerIds()).isEmpty();

      BigDecimal sum = result.memberAmounts().values().stream()
        .reduce(BigDecimal.ZERO, BigDecimal::add);
      assertThat(sum).isEqualByComparingTo(totalAmount);
    }
  }

  @Nested
  @DisplayName("결정론적 랜덤 검증")
  class DeterministicTests {

    @Test
    @DisplayName("동일한 시드로 동일한 사다리타기 결과가 나와야 한다")
    void distribute_sameSeeded_sameResult() {
      BigDecimal totalAmount = new BigDecimal("1000");
      List<Long> memberIds = List.of(1L, 2L, 3L);
      BigDecimal smallestUnit = BigDecimal.ONE;

      DistributionResult result1 = SettlementAmountDistributor.distribute(
        totalAmount, memberIds, smallestUnit, new Random(12345)
      );
      DistributionResult result2 = SettlementAmountDistributor.distribute(
        totalAmount, memberIds, smallestUnit, new Random(12345)
      );

      assertThat(result1.extraPayerIds()).isEqualTo(result2.extraPayerIds());
      assertThat(result1.memberAmounts()).isEqualTo(result2.memberAmounts());
    }
  }
}
