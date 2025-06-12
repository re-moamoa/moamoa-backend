package com.hbbhbank.moamoa.wallet.repository;

import com.hbbhbank.moamoa.wallet.domain.ExternalWalletTransaction;
import com.hbbhbank.moamoa.wallet.domain.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExternalWalletTransactionRepository extends JpaRepository<ExternalWalletTransaction, Long>, ExternalWalletTransactionRepositoryCustom {

}
