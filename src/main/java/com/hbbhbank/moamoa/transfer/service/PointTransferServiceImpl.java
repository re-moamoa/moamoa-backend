package com.hbbhbank.moamoa.transfer.service;

import com.hbbhbank.moamoa.global.exception.BaseException;
import com.hbbhbank.moamoa.transfer.dto.request.PointTransferRequestDto;
import com.hbbhbank.moamoa.transfer.dto.response.PointTransferResponseDto;
import com.hbbhbank.moamoa.transfer.exception.TransferErrorCode;
import com.hbbhbank.moamoa.wallet.domain.InternalWalletTransaction;
import com.hbbhbank.moamoa.wallet.domain.Wallet;
import com.hbbhbank.moamoa.wallet.domain.WalletTransactionStatus;
import com.hbbhbank.moamoa.wallet.domain.WalletTransactionType;
import com.hbbhbank.moamoa.wallet.repository.InternalWalletTransactionRepository;
import com.hbbhbank.moamoa.wallet.repository.WalletRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointTransferServiceImpl implements PointTransferService {

  private final WalletRepository walletRepository;
  private final InternalWalletTransactionRepository walletTransactionRepository;

  @Override
  @Transactional
  public PointTransferResponseDto transferByUserV1(PointTransferRequestDto dto) {
    Wallet fromWallet = walletRepository.findByWalletNumber(dto.fromWalletNumber())
      .orElseThrow(() -> new BaseException(TransferErrorCode.WALLET_NOT_FOUND));

    Wallet toWallet = walletRepository.findByWalletNumber(dto.toWalletNumber())
      .orElseThrow(() -> new BaseException(TransferErrorCode.WALLET_NOT_FOUND));

    // 동일 사용자 지갑 간 송금 차단
    if (fromWallet.getUser().getId().equals(toWallet.getUser().getId())) {
      throw new BaseException(TransferErrorCode.CANNOT_TRANSFER_TO_SELF);
    }

    // 통화 코드가 다른 경우 차단
    if (!fromWallet.getCurrency().equals(toWallet.getCurrency())) {
      throw new BaseException(TransferErrorCode.CURRENCY_MISMATCH);
    }

    // 잔액 부족 시 차단
    if (fromWallet.getBalance().compareTo(dto.amount()) < 0) {
      throw new BaseException(TransferErrorCode.INSUFFICIENT_BALANCE);
    }

    // 금액 이동
    fromWallet.decreaseBalance(dto.amount());
    toWallet.increaseBalance(dto.amount());

    // 거래 기록 저장
    InternalWalletTransaction transaction = InternalWalletTransaction.create(
      fromWallet,
      toWallet,
      WalletTransactionType.TRANSFER_OUT,
      WalletTransactionStatus.SUCCESS,
      dto.amount()
    );
    walletTransactionRepository.save(transaction);

    InternalWalletTransaction counterTransaction = InternalWalletTransaction.create(
      toWallet,
      fromWallet,
      WalletTransactionType.TRANSFER_IN,
      WalletTransactionStatus.SUCCESS,
      dto.amount()
    );
    walletTransactionRepository.save(counterTransaction);

    return new PointTransferResponseDto(
      toWallet.getUser().getName(),
      toWallet.getWalletNumber(),
      dto.amount(),
      toWallet.getCurrency().getName()
    );
  }

  @Override
  @Transactional
  public PointTransferResponseDto transferByUserV2(PointTransferRequestDto dto) {
    // 1) 검증용 조회 (락 없음)
    Wallet w1 = walletRepository.findByWalletNumber(dto.fromWalletNumber())
      .orElseThrow(() -> new BaseException(TransferErrorCode.WALLET_NOT_FOUND));
    Wallet w2 = walletRepository.findByWalletNumber(dto.toWalletNumber())
      .orElseThrow(() -> new BaseException(TransferErrorCode.WALLET_NOT_FOUND));

    if (w1.getUser().getId().equals(w2.getUser().getId())) {
      throw new BaseException(TransferErrorCode.CANNOT_TRANSFER_TO_SELF);
    }
    if (!w1.getCurrency().equals(w2.getCurrency())) {
      throw new BaseException(TransferErrorCode.CURRENCY_MISMATCH);
    }

    // 2) 락 순서 고정: ID 작은 쪽 먼저
    Wallet first = w1.getId().compareTo(w2.getId()) < 0 ? w1 : w2;
    Wallet second = first == w1 ? w2 : w1;

    // 3) 비관적 락 걸고 재조회
    first  = walletRepository.findByWalletNumberForUpdate(first.getWalletNumber())
      .orElseThrow(() -> new BaseException(TransferErrorCode.WALLET_NOT_FOUND));
    second = walletRepository.findByWalletNumberForUpdate(second.getWalletNumber())
      .orElseThrow(() -> new BaseException(TransferErrorCode.WALLET_NOT_FOUND));

    // 4) 실제 from/to 매핑
    Wallet fromWallet = dto.fromWalletNumber().equals(first.getWalletNumber()) ? first : second;
    Wallet toWallet   = fromWallet == first ? second : first;

    // 5) 잔액 부족 체크
    if (fromWallet.getBalance().compareTo(dto.amount()) < 0) {
      throw new BaseException(TransferErrorCode.INSUFFICIENT_BALANCE);
    }

    // 6) 금액 이동 (위에 락 걸었으니 안전)
    fromWallet.decreaseBalance(dto.amount());
    toWallet.increaseBalance(dto.amount());

    // 7) 거래 기록 저장
    walletTransactionRepository.save(InternalWalletTransaction.create(
      fromWallet, toWallet,
      WalletTransactionType.TRANSFER_OUT,
      WalletTransactionStatus.SUCCESS,
      dto.amount()
    ));
    walletTransactionRepository.save(InternalWalletTransaction.create(
      toWallet, fromWallet,
      WalletTransactionType.TRANSFER_IN,
      WalletTransactionStatus.SUCCESS,
      dto.amount()
    ));

    return new PointTransferResponseDto(
      toWallet.getUser().getName(),
      toWallet.getWalletNumber(),
      dto.amount(),
      toWallet.getCurrency().getName()
    );
  }

  @Override
  @Transactional
  public PointTransferResponseDto transferByUserV3(PointTransferRequestDto dto) {
    // 1) 한 번의 FOR UPDATE 조회로 두 지갑을 락 & 읽기
    List<Wallet> wallets = walletRepository.findByWalletNumberForUpdateV2(
      List.of(dto.fromWalletNumber(), dto.toWalletNumber())
    );
    if (wallets.size() != 2) {
      throw new BaseException(TransferErrorCode.WALLET_NOT_FOUND);
    }

    // 2) ORDER BY id 순으로 리턴되니 그대로 꺼내 쓰면 OK
    Wallet first  = wallets.get(0);
    Wallet second = wallets.get(1);

    // 3) 동일 유저 체크
    if (first.getUser().getId().equals(second.getUser().getId())) {
      throw new BaseException(TransferErrorCode.CANNOT_TRANSFER_TO_SELF);
    }

    // 4) 통화 코드 일치 체크
    if (!first.getCurrency().equals(second.getCurrency())) {
      throw new BaseException(TransferErrorCode.CURRENCY_MISMATCH);
    }

    // 5) from/to 매핑
    Wallet fromWallet = first.getWalletNumber().equals(dto.fromWalletNumber())
      ? first : second;
    Wallet toWallet   = fromWallet == first ? second : first;

    // 6) 잔액 충분 여부 (→ 이 balance 는 락 시점의 '진짜' 최신값)
    if (fromWallet.getBalance().compareTo(dto.amount()) < 0) {
      throw new BaseException(TransferErrorCode.INSUFFICIENT_BALANCE);
    }

    // 7) 금액 이동
    fromWallet.decreaseBalance(dto.amount());
    toWallet.increaseBalance(dto.amount());

    // 8) 거래 기록 저장 (double‐entry)
    walletTransactionRepository.save(InternalWalletTransaction.create(
      fromWallet,
      toWallet,
      WalletTransactionType.TRANSFER_OUT,
      WalletTransactionStatus.SUCCESS,
      dto.amount()
    ));
    walletTransactionRepository.save(InternalWalletTransaction.create(
      toWallet,
      fromWallet,
      WalletTransactionType.TRANSFER_IN,
      WalletTransactionStatus.SUCCESS,
      dto.amount()
    ));

    // 9) 응답 생성
    return new PointTransferResponseDto(
      toWallet.getUser().getName(),
      toWallet.getWalletNumber(),
      dto.amount(),
      toWallet.getCurrency().getName()
    );
  }
}
