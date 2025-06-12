package com.hbbhbank.moamoa.payment.dto.response;

public record QrCodeCreateResponseDto(
  Long qrImageId,
  String qrImageUrl
) {}
