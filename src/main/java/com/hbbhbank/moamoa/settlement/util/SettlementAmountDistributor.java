package com.hbbhbank.moamoa.settlement.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 정산 금액 분배 유틸리티.
 *
 * 총 금액을 인원수로 나눌 때 발생하는 나머지를
 * 사다리타기(랜덤) 방식으로 공정하게 분배한다.
 *
 * @see <a href="https://story.kakaopay.com/05-kakaopay-money/">카카오페이 정산하기 — 사다리타기 기능</a>
 */
public class SettlementAmountDistributor {

  /**
   * 금액 분배 결과를 담는 불변 객체.
   *
   * @param baseAmount     기본 분담액 (RoundingMode.DOWN 적용)
   * @param remainder      나머지 금액 (totalAmount - baseAmount * memberCount)
   * @param memberAmounts  멤버별 실제 분담액 (userId → amount)
   * @param extraPayerIds  나머지를 추가 부담하는 멤버 ID 목록 (사다리타기 당첨자)
   */
  public record DistributionResult(
    BigDecimal baseAmount,
    BigDecimal remainder,
    Map<Long, BigDecimal> memberAmounts,
    List<Long> extraPayerIds
  ) {}

  /**
   * 총 금액을 멤버들에게 분배한다.
   * 나머지가 발생하면 랜덤으로 선정된 멤버에게 최소 단위씩 추가 배분한다.
   *
   * <pre>
   * 예시 (KRW, smallestUnit=1):
   *   1000원 / 3명 → baseAmount=333, remainder=1
   *   → 랜덤 1명에게 334원, 나머지 2명은 333원
   *
   * 예시 (USD, smallestUnit=0.01):
   *   $10.00 / 3명 → baseAmount=3.33, remainder=0.01
   *   → 랜덤 1명에게 $3.34, 나머지 2명은 $3.33
   * </pre>
   *
   * @param totalAmount    총 정산 금액
   * @param memberUserIds  분배 대상 멤버 ID 목록 (방장 포함)
   * @param smallestUnit   통화별 최소 단위 (KRW→1, USD→0.01)
   * @param random         랜덤 인스턴스 (테스트 시 시드 고정 가능)
   * @return 분배 결과
   */
  public static DistributionResult distribute(
    BigDecimal totalAmount,
    List<Long> memberUserIds,
    BigDecimal smallestUnit,
    Random random
  ) {
    int memberCount = memberUserIds.size();

    // scale은 smallestUnit의 소수점 자릿수에 맞춤 (KRW→0, USD→2)
    int scale = smallestUnit.stripTrailingZeros().scale();
    if (scale < 0) scale = 0;

    BigDecimal baseAmount = totalAmount.divide(
      BigDecimal.valueOf(memberCount), scale, RoundingMode.DOWN
    );

    BigDecimal remainder = totalAmount.subtract(
      baseAmount.multiply(BigDecimal.valueOf(memberCount))
    );

    // 나머지를 최소 단위로 나눈 횟수 = 추가 부담자 수
    int extraCount = remainder.divide(smallestUnit, 0, RoundingMode.DOWN).intValue();

    // 사다리타기: 멤버 목록을 셔플하여 extraCount명 선정
    List<Long> shuffled = new ArrayList<>(memberUserIds);
    Collections.shuffle(shuffled, random);
    List<Long> extraPayerIds = extraCount > 0
      ? List.copyOf(shuffled.subList(0, extraCount))
      : List.of();

    Set<Long> extraPayerSet = new HashSet<>(extraPayerIds);

    Map<Long, BigDecimal> memberAmounts = new LinkedHashMap<>();
    for (Long userId : memberUserIds) {
      if (extraPayerSet.contains(userId)) {
        memberAmounts.put(userId, baseAmount.add(smallestUnit));
      } else {
        memberAmounts.put(userId, baseAmount);
      }
    }

    return new DistributionResult(baseAmount, remainder, memberAmounts, extraPayerIds);
  }
}
