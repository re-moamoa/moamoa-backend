package com.hbbhbank.moamoa.wallet.repository;

import com.hbbhbank.moamoa.wallet.domain.Currency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CurrencyRepository extends JpaRepository<Currency, Long> {
  Optional<Currency> findByCode(String code);
}
