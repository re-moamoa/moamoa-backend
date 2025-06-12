package com.hbbhbank.moamoa.wallet.repository;

import com.hbbhbank.moamoa.wallet.domain.HwanbeeAccountLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HwanbeeLinkRepository extends JpaRepository<HwanbeeAccountLink, Long> {
  Optional<HwanbeeAccountLink> findByUserIdAndHwanbeeBankAccountNumber(Long userId, String accountNumber);

  List<HwanbeeAccountLink> findByUserId(Long userId);

  List<HwanbeeAccountLink> findByUserIdAndCurrencyCode(Long userId, String currencyCode);
}