package com.hbbhbank.moamoa.withdraw.service;

import com.hbbhbank.moamoa.external.auth.OAuth2TokenService;
import com.hbbhbank.moamoa.external.client.HwanbeeRemittanceClient;
import com.hbbhbank.moamoa.external.dto.request.transfer.HwanbeeRemittanceRequestDto;
import com.hbbhbank.moamoa.external.exception.HwanbeeErrorCode;
import com.hbbhbank.moamoa.global.exception.BaseException;
import com.hbbhbank.moamoa.global.security.util.SecurityUtil;
import com.hbbhbank.moamoa.transfer.exception.TransferErrorCode;
import com.hbbhbank.moamoa.user.domain.User;
import com.hbbhbank.moamoa.user.exception.UserErrorCode;
import com.hbbhbank.moamoa.user.repository.UserRepository;
import com.hbbhbank.moamoa.wallet.domain.*;
import com.hbbhbank.moamoa.wallet.repository.ExternalWalletTransactionRepository;
import com.hbbhbank.moamoa.wallet.repository.HwanbeeLinkRepository;
import com.hbbhbank.moamoa.wallet.repository.WalletRepository;
import com.hbbhbank.moamoa.withdraw.dto.request.PointWithdrawRequestDto;
import com.hbbhbank.moamoa.withdraw.dto.response.PointWithdrawResponseDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
@Service
@RequiredArgsConstructor
public class WithdrawServiceImpl implements WithdrawService {

  private final WalletRepository walletRepository;
  private final ExternalWalletTransactionRepository externalWalletTransactionRepository;
  private final HwanbeeRemittanceClient hwanbeeRemittanceClient;
  private final OAuth2TokenService oAuth2TokenService;
  private final UserRepository userRepository;
  private final HwanbeeLinkRepository hwanbeeLinkRepository;

  private static final String MOAMOA_CORPORATE_ACCOUNT = "15002-402-000009";

  @Override
  @Transactional
  public PointWithdrawResponseDto withdraw(PointWithdrawRequestDto dto) {
    Long userId = SecurityUtil.getCurrentUserId();

    User user = userRepository.findById(userId)
      .orElseThrow(() -> new BaseException(UserErrorCode.USER_NOT_FOUND));

    String accessToken = oAuth2TokenService.ensureAccessToken(user);

    Wallet userWallet = walletRepository.findByUserId(userId)
      .orElseThrow(() -> new BaseException(TransferErrorCode.WALLET_NOT_FOUND));

    if (userWallet.getBalance().compareTo(dto.amount()) < 0) {
      throw new BaseException(TransferErrorCode.INSUFFICIENT_BALANCE);
    }

    HwanbeeAccountLink linkedAccount = hwanbeeLinkRepository.findByUserIdAndHwanbeeBankAccountNumber(userId, dto.bankAccount())
      .orElseThrow(() -> new BaseException(HwanbeeErrorCode.ACCOUNT_LINK_FAILED));

    userWallet.decreaseBalance(dto.amount());

    ExternalWalletTransaction transaction = ExternalWalletTransaction.create(
      userWallet,
      linkedAccount,
      WalletTransactionType.WITHDRAWAL,
      WalletTransactionStatus.SUCCESS,
      dto.amount()
    );
    ExternalWalletTransaction saved = externalWalletTransactionRepository.save(transaction);

    hwanbeeRemittanceClient.remitFromUserAccount(
      HwanbeeRemittanceRequestDto.builder()
        .fromAccountNumber(dto.bankAccount())
        .toAccountNumber(MOAMOA_CORPORATE_ACCOUNT)
        .amount(dto.amount())
        .currency(userWallet.getCurrency().getName())
        .description("포인트 환불")
        .partnerTransactionId("moamoa-withdraw-" + saved.getId())
        .requestedAt(ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
        .build(),
      accessToken
    );

    return new PointWithdrawResponseDto(
      saved.getId(),
      userWallet.getWalletNumber(),
      dto.bankAccount(),
      saved.getAmount(),
      userWallet.getCurrency().getName()
    );
  }
}
