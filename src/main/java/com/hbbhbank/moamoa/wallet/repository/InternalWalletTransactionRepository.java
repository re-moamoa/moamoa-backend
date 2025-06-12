package com.hbbhbank.moamoa.wallet.repository;

import com.hbbhbank.moamoa.wallet.domain.InternalWalletTransaction;
import com.hbbhbank.moamoa.wallet.domain.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InternalWalletTransactionRepository extends JpaRepository<InternalWalletTransaction, Long>, InternalWalletTransactionRepositoryCustom {
}