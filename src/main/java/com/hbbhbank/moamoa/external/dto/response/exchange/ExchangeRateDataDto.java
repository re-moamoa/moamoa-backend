package com.hbbhbank.moamoa.external.dto.response.exchange;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public record ExchangeRateDataDto(
  String currency,
  String registrationTime,
  String bankOfKoreaRate
) implements Serializable {
  @JsonCreator
  public ExchangeRateDataDto(
    @JsonProperty("currency") String currency,
    @JsonProperty("registrationTime") String registrationTime,
    @JsonProperty("bankOfKoreaRate") String bankOfKoreaRate
  ) {
    this.currency = currency;
    this.registrationTime = registrationTime;
    this.bankOfKoreaRate = bankOfKoreaRate;
  }
}
