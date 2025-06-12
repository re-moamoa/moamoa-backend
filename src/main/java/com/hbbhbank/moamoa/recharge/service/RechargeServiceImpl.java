package com.hbbhbank.moamoa.recharge.service;

import com.hbbhbank.moamoa.external.auth.OAuth2TokenService;
import com.hbbhbank.moamoa.external.client.HwanbeeRemittanceClient;
import com.hbbhbank.moamoa.external.dto.request.transfer.HwanbeeRemittanceRequestDto;
import com.hbbhbank.moamoa.external.exception.HwanbeeErrorCode;
import com.hbbhbank.moamoa.global.exception.BaseException;
import com.hbbhbank.moamoa.global.security.util.SecurityUtil;
import com.hbbhbank.moamoa.recharge.dto.request.RechargeRequestDto;
import com.hbbhbank.moamoa.recharge.dto.response.RechargeResponseDto;
import com.hbbhbank.moamoa.transfer.exception.TransferErrorCode;
import com.hbbhbank.moamoa.user.domain.User;
import com.hbbhbank.moamoa.user.exception.UserErrorCode;
import com.hbbhbank.moamoa.user.repository.UserRepository;
import com.hbbhbank.moamoa.wallet.domain.*;
import com.hbbhbank.moamoa.wallet.repository.ExternalWalletTransactionRepository;
import com.hbbhbank.moamoa.wallet.repository.HwanbeeLinkRepository;
import com.hbbhbank.moamoa.wallet.repository.WalletRepository;
import com.hbbhbank.moamoa.wallet.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RechargeServiceImpl implements RechargeService {

  private final WalletRepository walletRepository;
  private final ExternalWalletTransactionRepository externalWalletTransactionRepository;
  private final HwanbeeRemittanceClient hwanbeeRemittanceClient;
  private final OAuth2TokenService oAuth2TokenService;
  private final UserRepository userRepository;
  private final HwanbeeLinkRepository hwanbeeLinkRepository;
  private final CurrencyService currencyService;

  private static final String MOAMOA_CORPORATE_ACCOUNT = "15002-402-000009";

  @Override
  public RechargeResponseDto charge(RechargeRequestDto dto) {
    Long userId = SecurityUtil.getCurrentUserId();

    User user = userRepository.findById(userId)
      .orElseThrow(() -> new BaseException(UserErrorCode.USER_NOT_FOUND));

    String accessToken = oAuth2TokenService.ensureAccessToken(user);

    Currency currency = currencyService.getByCodeOrThrow(dto.currencyCode());

    Wallet wallet = walletRepository.findByUserIdAndCurrency(userId, currency)
      .orElseThrow(() -> new BaseException(TransferErrorCode.WALLET_NOT_FOUND));

    HwanbeeAccountLink hwanbeeAccount = hwanbeeLinkRepository.findByUserIdAndHwanbeeBankAccountNumber(userId, dto.hwanbeeAccountNumber())
      .orElseThrow(() -> new BaseException(HwanbeeErrorCode.ACCOUNT_LINK_FAILED));

    // 환비 송금 요청
    hwanbeeRemittanceClient.remitFromUserAccount(
      HwanbeeRemittanceRequestDto.builder()
        .fromAccountNumber(dto.hwanbeeAccountNumber())
        .toAccountNumber(MOAMOA_CORPORATE_ACCOUNT)
        .amount(dto.amount())
        .currency(wallet.getCurrency().getCode())
        .description("모아모아 포인트 충전")
        .partnerTransactionId("tx-" + UUID.randomUUID())
        .build(),
      accessToken
    );

    wallet.increaseBalance(dto.amount());

    ExternalWalletTransaction transaction = ExternalWalletTransaction.create(
      wallet,
      hwanbeeAccount,
      WalletTransactionType.CHARGE,
      WalletTransactionStatus.SUCCESS,
      dto.amount()
    );
    externalWalletTransactionRepository.save(transaction);

    return new RechargeResponseDto(
      wallet.getWalletNumber(),
      dto.amount(),
      wallet.getCurrency().getName()
    );
  }
}

