package com.hbbhbank.moamoa.wallet.service;

import com.hbbhbank.moamoa.wallet.domain.ExternalWalletTransaction;
import com.hbbhbank.moamoa.wallet.domain.InternalWalletTransaction;
import com.hbbhbank.moamoa.wallet.dto.request.transaction.TransactionFilterRequestDto;
import com.hbbhbank.moamoa.wallet.dto.response.transaction.TransactionResponseDto;
import com.hbbhbank.moamoa.wallet.repository.ExternalWalletTransactionRepository;
import com.hbbhbank.moamoa.wallet.repository.InternalWalletTransactionRepository;
import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static com.hbbhbank.moamoa.wallet.domain.QInternalWalletTransaction.internalWalletTransaction;
import static com.hbbhbank.moamoa.wallet.domain.QExternalWalletTransaction.externalWalletTransaction;

@Service
@RequiredArgsConstructor
public class WalletTransactionServiceImpl implements WalletTransactionService {

  private final InternalWalletTransactionRepository internalRepo;
  private final ExternalWalletTransactionRepository externalRepo;

  @Override
  public Page<TransactionResponseDto> findAll(TransactionFilterRequestDto filter, Long userId) {
    BooleanBuilder internalCond = new BooleanBuilder();
    BooleanBuilder externalCond = new BooleanBuilder();

    // 사용자 본인의 지갑만 조회
    internalCond.and(internalWalletTransaction.wallet.user.id.eq(userId));
    externalCond.and(externalWalletTransaction.wallet.user.id.eq(userId));

    if (filter.walletId() != null) {
      internalCond.and(internalWalletTransaction.wallet.id.eq(filter.walletId()));
      externalCond.and(externalWalletTransaction.wallet.id.eq(filter.walletId()));
    }

    if (filter.currencyCode() != null) {
      internalCond.and(internalWalletTransaction.wallet.currency.code.eq(filter.currencyCode()));
      externalCond.and(externalWalletTransaction.wallet.currency.code.eq(filter.currencyCode()));
    }

    if (filter.type() != null) {
      internalCond.and(internalWalletTransaction.type.eq(filter.type()));
      externalCond.and(externalWalletTransaction.type.eq(filter.type()));
    }

    if (filter.startDate() != null && filter.endDate() != null) {
      internalCond.and(internalWalletTransaction.transactedAt.between(filter.startDate(), filter.endDate()));
      externalCond.and(externalWalletTransaction.transactedAt.between(filter.startDate(), filter.endDate()));
    }

    List<TransactionResponseDto> internals = internalRepo.findAllByPredicate(internalCond).stream()
      .map(tx -> new TransactionResponseDto(
        tx.getId(),
        tx.getWallet().getWalletNumber(),
        tx.getCounterWallet() != null ? tx.getCounterWallet().getWalletNumber() : null,
        tx.getWallet().getCurrency().getCode(),
        tx.getType(),
        tx.getStatus(),
        tx.getAmount(),
        tx.getTransactedAt(),
        false
      )).toList();

    List<TransactionResponseDto> externals = externalRepo.findAllByPredicate(externalCond).stream()
      .map(tx -> new TransactionResponseDto(
        tx.getId(),
        tx.getWallet().getWalletNumber(),
        null,
        tx.getWallet().getCurrency().getCode(),
        tx.getType(),
        tx.getStatus(),
        tx.getAmount(),
        tx.getTransactedAt(),
        true
      )).toList();

    List<TransactionResponseDto> merged = Stream.concat(internals.stream(), externals.stream())
      .sorted(Comparator.comparing(TransactionResponseDto::transactedAt).reversed())
      .toList();

    int page = filter.page() != null ? filter.page() : 0;
    int size = filter.size() != null ? filter.size() : 20;
    int start = Math.min(page * size, merged.size());
    int end = Math.min(start + size, merged.size());

    return new PageImpl<>(merged.subList(start, end), PageRequest.of(page, size), merged.size());
  }

  @Override
  public TransactionResponseDto findLatest(Long userId) {
    BooleanBuilder internalCond = new BooleanBuilder()
      .and(internalWalletTransaction.wallet.user.id.eq(userId));
    BooleanBuilder externalCond = new BooleanBuilder()
      .and(externalWalletTransaction.wallet.user.id.eq(userId));

    InternalWalletTransaction internal = internalRepo.findAllByPredicate(internalCond).stream()
      .max(Comparator.comparing(InternalWalletTransaction::getTransactedAt))
      .orElse(null);

    ExternalWalletTransaction external = externalRepo.findAllByPredicate(externalCond).stream()
      .max(Comparator.comparing(ExternalWalletTransaction::getTransactedAt))
      .orElse(null);

    TransactionResponseDto iRes = internal == null ? null :
      new TransactionResponseDto(
        internal.getId(),
        internal.getWallet().getWalletNumber(),
        internal.getCounterWallet() != null ? internal.getCounterWallet().getWalletNumber() : null,
        internal.getWallet().getCurrency().getCode(),
        internal.getType(),
        internal.getStatus(),
        internal.getAmount(),
        internal.getTransactedAt(),
        false
      );

    TransactionResponseDto eRes = external == null ? null :
      new TransactionResponseDto(
        external.getId(),
        external.getWallet().getWalletNumber(),
        null,
        external.getWallet().getCurrency().getCode(),
        external.getType(),
        external.getStatus(),
        external.getAmount(),
        external.getTransactedAt(),
        true
      );

    if (iRes == null && eRes == null) return null;
    if (iRes == null) return eRes;
    if (eRes == null) return iRes;
    return iRes.transactedAt().isAfter(eRes.transactedAt()) ? iRes : eRes;
  }
}
