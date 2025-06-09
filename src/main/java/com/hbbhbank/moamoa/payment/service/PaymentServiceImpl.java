package com.hbbhbank.moamoa.payment.service;

import com.hbbhbank.moamoa.global.exception.BaseException;
import com.hbbhbank.moamoa.payment.domain.QrImage;
import com.hbbhbank.moamoa.payment.dto.request.PaymentRequestDto;
import com.hbbhbank.moamoa.payment.dto.response.QrCodeCreateResponseDto;
import com.hbbhbank.moamoa.payment.dto.response.QrCodeInfoResponseDto;
import com.hbbhbank.moamoa.payment.exception.PaymentErrorCode;
import com.hbbhbank.moamoa.payment.repository.QrImageRepository;
import com.hbbhbank.moamoa.payment.util.QRCodeUtil;
import com.hbbhbank.moamoa.user.domain.User;
import com.hbbhbank.moamoa.user.service.UserService;
import com.hbbhbank.moamoa.wallet.domain.InternalWalletTransaction;
import com.hbbhbank.moamoa.wallet.domain.Wallet;
import com.hbbhbank.moamoa.wallet.domain.WalletTransactionStatus;
import com.hbbhbank.moamoa.wallet.domain.WalletTransactionType;
import com.hbbhbank.moamoa.wallet.exception.WalletErrorCode;
import com.hbbhbank.moamoa.wallet.repository.InternalWalletTransactionRepository;
import com.hbbhbank.moamoa.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

  private final QrImageRepository qrImageRepository;
  private final WalletRepository walletRepository;
  private final InternalWalletTransactionRepository internalWalletTransactionRepository;
  private final UserService userService;

  @Override
  @Transactional
  public QrCodeCreateResponseDto generateQr(Long walletId) {
    // 1. 지갑 조회
    Wallet wallet = walletRepository.findById(walletId)
      .orElseThrow(() -> new BaseException(WalletErrorCode.NOT_FOUND_WALLET));

    // 2. uuid 및 링크 생성
    String uuid = UUID.randomUUID().toString();
    String qrLink = "https://moamoa.hwanbee.site/api/v1/payments/use/" + uuid;

    // 3. QR 이미지 생성
    byte[] imageBytes = QRCodeUtil.generateQRCodeImage(qrLink);

    // 4. DB 저장
    QrImage savedQrImage = qrImageRepository.save(
      QrImage.builder()
        .uuid(uuid)
        .qrImage(imageBytes)
        .wallet(wallet)
        .build()
    );

    // 5. 응답
    return new QrCodeCreateResponseDto(savedQrImage.getId(), qrLink);
  }

  @Override
  @Transactional
  public void payWithQr(String uuid, PaymentRequestDto req) {

    Long buyerUserId = userService.getCurrentUserId();

    QrImage qrImage = qrImageRepository.findByUuid(uuid)
      .orElseThrow(() -> new BaseException(PaymentErrorCode.FAILED_CREATE_QR));

    if (qrImage.getExpiresAt().isBefore(LocalDateTime.now())) {
      throw new BaseException(PaymentErrorCode.QR_EXPIRED);
    }

    req.validate();

    Wallet sellerWallet = qrImage.getWallet();
    Wallet buyerWallet = walletRepository.findByUserIdAndCurrencyCode(buyerUserId, req.currencyCode())
      .orElseThrow(() -> new BaseException(WalletErrorCode.NOT_FOUND_WALLET));

    BigDecimal amount = req.amount();

    if (buyerWallet.getBalance().compareTo(amount) < 0) {
      throw new BaseException(WalletErrorCode.INSUFFICIENT_BALANCE);
    }

    buyerWallet.decreaseBalance(amount);
    sellerWallet.increaseBalance(amount);
    walletRepository.saveAll(List.of(buyerWallet, sellerWallet));

    internalWalletTransactionRepository.save(
      InternalWalletTransaction.create(
        buyerWallet, sellerWallet, WalletTransactionType.QR_PAYMENT, WalletTransactionStatus.SUCCESS, amount.negate()
      )
    );
    internalWalletTransactionRepository.save(
      InternalWalletTransaction.create(
        sellerWallet, buyerWallet, WalletTransactionType.QR_PAYMENT, WalletTransactionStatus.SUCCESS, amount
      )
    );
  }

  /**
   * QR 이미지 조회
   */
  @Override
  @Transactional(readOnly = true)
  public byte[] getQRCodeImage(Long qrId) {
    return qrImageRepository.findById(qrId)
      .orElseThrow(() -> new BaseException(PaymentErrorCode.FAILED_CREATE_QR))
      .getQrImage();
  }

  /**
   * QR 재발급
   */
  @Override
  @Transactional
  public QrCodeCreateResponseDto reissueQr(Long walletId) {
    return generateQr(walletId);
  }

  /**
   * QR 정보 조회
   */
  @Override
  @Transactional(readOnly = true)
  public QrCodeInfoResponseDto getQrInfo(String uuid) {
    // 1. UUID로 QR 이미지 조회
    QrImage qrImage = qrImageRepository.findByUuid(uuid)
      .orElseThrow(() -> new BaseException(PaymentErrorCode.FAILED_CREATE_QR));

    // 2. QR 이미지가 만료되었는지 확인
    if (qrImage.getExpiresAt().isBefore(LocalDateTime.now())) {
      throw new BaseException(PaymentErrorCode.QR_EXPIRED);
    }

    Wallet wallet = qrImage.getWallet(); // QR 이미지에 연결된 지갑 정보
    User owner = wallet.getUser(); // 지갑 소유자 정보

    return new QrCodeInfoResponseDto(
      wallet.getId(), // QR 이미지가 연결된 지갑 ID
      owner.getName(), // 지갑 소유자 이름
      wallet.getCurrency().getCode() // 지갑 통화 코드
    );
  }
}
