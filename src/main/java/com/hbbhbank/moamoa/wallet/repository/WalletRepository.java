package com.hbbhbank.moamoa.wallet.repository;

import com.hbbhbank.moamoa.wallet.domain.Currency;
import com.hbbhbank.moamoa.wallet.domain.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long>, WalletRepositoryCustom {
  Optional<Wallet> findByUserId(Long userId);
  Optional<Wallet> findByUserIdAndCurrency(Long userId, Currency currencyCode);
}

