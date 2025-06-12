package com.hbbhbank.moamoa.wallet.dto.request.transaction;

import com.hbbhbank.moamoa.wallet.domain.WalletTransactionType;

import java.time.LocalDateTime;

public record TransactionFilterRequestDto(
  Long walletId,                // 지갑 ID (지갑별/통화별)
  String currencyCode,          // 통화코드 (통화별)
  WalletTransactionType type,   // 거래구분 (ex: CHARGE, TRANSFER_OUT 등)
  LocalDateTime startDate,      // 기간-시작
  LocalDateTime endDate,        // 기간-끝
  Integer page,                 // 페이지 번호
  Integer size                  // 페이지 크기
) {
}
