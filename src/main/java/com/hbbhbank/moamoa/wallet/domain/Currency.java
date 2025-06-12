package com.hbbhbank.moamoa.wallet.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "currencies")
public class Currency {

  @Id
  @Column(name = "currency_code", length = 10)
  private String code; // 통화 코드 (ex. KRW, USD 등)

  @Column(name = "currency_name", length = 50, nullable = false)
  private String name; // '대한민국 원화'와 같은 전체 이름

  @Column(name = "is_foreign", nullable = false)
  private boolean isForeign; // 외화 여부 (true: 외화, false: 원화)

  @Builder
  public Currency(String code, String name, boolean isForeign) {
    this.code = code;
    this.name = name;
    this.isForeign = isForeign;
  }

  // 통화 코드 기반 동등성 비교
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Currency)) return false;
    Currency currency = (Currency) o;
    return code.equals(currency.code);
  }

  @Override
  public int hashCode() {
    return code.hashCode();
  }
}
