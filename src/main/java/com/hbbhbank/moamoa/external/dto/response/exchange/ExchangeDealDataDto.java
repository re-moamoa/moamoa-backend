package com.hbbhbank.moamoa.external.dto.response.exchange;

public record ExchangeDealDataDto(
  String krwAccount,
  String fcyAccount
) {
  public static ExchangeDealDataDto from(String krwAccount, String fcyAccount) {
    return new ExchangeDealDataDto(krwAccount, fcyAccount);
  }
}
