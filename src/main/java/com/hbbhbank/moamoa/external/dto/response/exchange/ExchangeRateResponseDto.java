package com.hbbhbank.moamoa.external.dto.response.exchange;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

public record ExchangeRateResponseDto(
  int status,
  String message,
  List<ExchangeRateDataDto> data
) implements Serializable {
  @JsonCreator
  public ExchangeRateResponseDto(
    @JsonProperty("status") int status,
    @JsonProperty("message") String message,
    @JsonProperty("data") List<ExchangeRateDataDto> data
  ) {
    this.status = status;
    this.message = message;
    this.data = data;
  }
}