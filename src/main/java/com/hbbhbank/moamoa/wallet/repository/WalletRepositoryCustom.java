package com.hbbhbank.moamoa.wallet.repository;

import com.hbbhbank.moamoa.wallet.domain.Wallet;

import java.util.List;
import java.util.Optional;

public interface WalletRepositoryCustom {

  Optional<Wallet> findByUserIdAndCurrencyCode(Long userId, String currencyCode);

  boolean existsByUserIdAndCurrencyCode(Long userId, String currencyCode);

  List<Wallet> findAllByUser(Long userId);

  // 기존 조회
  Optional<Wallet> findByWalletNumber(String walletNumber);

  // 락 적용 조회 (동시성 제어)
  Optional<Wallet> findByWalletNumberForUpdate(String walletNumber);

  List<Wallet> findByWalletNumberForUpdateV2(List<String> walletNumbers);
}
