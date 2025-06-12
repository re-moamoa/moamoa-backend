package com.hbbhbank.moamoa.external.dto.response.account;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record VerificationAccountDataDto(
  Boolean verified,
  String accountNumber,
  String accountStatus,
  String currencyCode,
  String productCode,
  LocalDate openDate,
  LocalDate maturityDate,
  String accountType,
  String message
) {}
