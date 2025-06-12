package com.hbbhbank.moamoa.payment.dto.response;

public record QrCodeInfoResponseDto(
  Long walletId,
  String ownerName,
  String currencyCode
) {}

