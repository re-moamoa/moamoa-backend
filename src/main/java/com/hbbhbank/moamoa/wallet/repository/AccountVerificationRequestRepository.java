package com.hbbhbank.moamoa.wallet.repository;

import com.hbbhbank.moamoa.user.domain.User;
import com.hbbhbank.moamoa.wallet.domain.AccountVerificationRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountVerificationRequestRepository extends JpaRepository<AccountVerificationRequest, Long> {
  Optional<AccountVerificationRequest> findTopByUser_IdOrderByCreatedAtDesc(Long userId);
}

