package com.hbbhbank.moamoa.wallet.service;

import com.hbbhbank.moamoa.wallet.dto.request.transaction.TransactionFilterRequestDto;
import com.hbbhbank.moamoa.wallet.dto.response.transaction.TransactionResponseDto;
import org.springframework.data.domain.Page;

public interface WalletTransactionService {

  // 전체 거래내역 (필터 포함)
  Page<TransactionResponseDto> findAll(TransactionFilterRequestDto filter, Long userId);

  // 가장 최근 거래 1건
  TransactionResponseDto findLatest(Long userId);
}

