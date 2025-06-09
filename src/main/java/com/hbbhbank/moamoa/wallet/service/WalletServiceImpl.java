package com.hbbhbank.moamoa.wallet.service;

import com.hbbhbank.moamoa.external.auth.OAuth2TokenService;
import com.hbbhbank.moamoa.external.client.HwanbeeAccountClient;
import com.hbbhbank.moamoa.external.dto.request.account.VerificationCheckRequestDto;
import com.hbbhbank.moamoa.external.dto.request.account.VerificationCodeRequestDto;
import com.hbbhbank.moamoa.external.dto.response.account.VerificationAccountDataDto;
import com.hbbhbank.moamoa.external.dto.response.account.VerificationCheckResponseDto;
import com.hbbhbank.moamoa.external.dto.response.account.VerificationCodeResponseDto;
import com.hbbhbank.moamoa.global.exception.BaseException;
import com.hbbhbank.moamoa.user.domain.User;
import com.hbbhbank.moamoa.user.exception.UserErrorCode;
import com.hbbhbank.moamoa.user.repository.UserRepository;
import com.hbbhbank.moamoa.user.service.UserService;
import com.hbbhbank.moamoa.wallet.domain.AccountVerificationRequest;
import com.hbbhbank.moamoa.wallet.domain.Currency;
import com.hbbhbank.moamoa.wallet.domain.HwanbeeAccountLink;
import com.hbbhbank.moamoa.wallet.domain.Wallet;
import com.hbbhbank.moamoa.wallet.dto.request.wallet.SearchWalletRequestDto;
import com.hbbhbank.moamoa.wallet.dto.response.wallet.BankAccountResponseDto;
import com.hbbhbank.moamoa.wallet.dto.response.wallet.CreateWalletResponseDto;
import com.hbbhbank.moamoa.wallet.dto.response.wallet.SearchWalletResponseDto;
import com.hbbhbank.moamoa.wallet.exception.WalletErrorCode;
import com.hbbhbank.moamoa.wallet.repository.AccountVerificationRequestRepository;
import com.hbbhbank.moamoa.wallet.repository.HwanbeeLinkRepository;
import com.hbbhbank.moamoa.wallet.repository.WalletRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

  private final WalletRepository walletRepository;
  private final UserService userService;
  private final HwanbeeAccountClient hwanbeeAccountClient;
  private final AccountVerificationRequestRepository accountVerificationRequestRepository;
  private final HwanbeeLinkRepository hwanbeeLinkRepository;
  private final CurrencyService currencyService;
  private final UserRepository userRepository;
  private final OAuth2TokenService oAuth2TokenClient;

  /**
   * 환비에 인증코드 발급 요청
   */
  @Override
  public void requestVerificationCode(VerificationCodeRequestDto req) {
    Long userId = userService.getCurrentUserId();
    User user = userRepository.findById(userId)
      .orElseThrow(() -> new BaseException(UserErrorCode.USER_NOT_FOUND));

    // access token이 없거나 만료되었으면 발급
    String accessToken = oAuth2TokenClient.ensureAccessToken(user);

    // 환비 API 호출 - 1원 송금 요청
    VerificationCodeResponseDto response = hwanbeeAccountClient.requestVerificationCode(req, accessToken);

    String transactionId = response.data().transactionId();

    // 추후 확인용으로 인증 요청 저장
    accountVerificationRequestRepository.save(AccountVerificationRequest.from(transactionId, user));
  }

  /**
   * 환비 계좌 인증이 완료된 후 지갑 생성
   */
  @Override
  @Transactional
  public CreateWalletResponseDto createWalletAfterVerification(String inputCode) {
    Long userId = userService.getCurrentUserId();
    User user = userRepository.findById(userId)
      .orElseThrow(() -> new BaseException(UserErrorCode.USER_NOT_FOUND));

    String accessToken = oAuth2TokenClient.ensureAccessToken(user);

    // 가장 최근의 인증 요청(transactionId) 가져오기
    AccountVerificationRequest request = accountVerificationRequestRepository
      .findTopByUser_IdOrderByCreatedAtDesc(userId)
      .orElseThrow(() -> new BaseException(WalletErrorCode.NOT_FOUND_VERIFICATION_REQUEST));

    // 입력 코드로 인증 확인
    VerificationCheckRequestDto checkRequest = new VerificationCheckRequestDto(request.getTransactionId(), inputCode);

    // access token이 없거나 만료되었으면 발급
    VerificationCheckResponseDto checkResponse = hwanbeeAccountClient.verifyInputCode(checkRequest, accessToken);

    VerificationAccountDataDto data = checkResponse.data();
    if (data == null || !Boolean.TRUE.equals(data.verified())) {
      throw new BaseException(WalletErrorCode.FAIL_VERIFICATION);
    }

    // 사용자 및 통화 정보 조회
    userService.getByIdOrThrow(userId);
    Currency currency = currencyService.getByCodeOrThrow(data.currencyCode());

    // 중복된 환비 계좌가 없을 경우 새로 저장
    HwanbeeAccountLink accountLink = hwanbeeLinkRepository
      .findByUserIdAndHwanbeeBankAccountNumber(userId, data.accountNumber())
      .orElseGet(() -> {
        HwanbeeAccountLink link = HwanbeeAccountLink.create(
          userId,
          data.accountNumber(), // 환비 계좌 정보
          data.currencyCode()
        );
        return hwanbeeLinkRepository.save(link);
      });

    // 이미 동일 통화 지갑이 있으면 예외
    if (walletRepository.existsByUserIdAndCurrencyCode(userId, data.currencyCode())) {
      throw new BaseException(WalletErrorCode.DUPLICATE_WALLET);
    }

    // 지갑 생성 및 연관관계 설정
    String walletNumber = generateWalletNumber();
    Wallet wallet = Wallet.create(user, walletNumber, currency, accountLink);

    // 양방향 연관관계 설정
    user.addWallet(wallet);
    // 저장
    walletRepository.save(wallet);

    return CreateWalletResponseDto.from(data.accountNumber());
  }

  /**
   * 환비 계좌 인증이 완료된 후 계좌 연결
   */
  @Override
  @Transactional
  public BankAccountResponseDto searchHwannbeeAccount(String inputCode) {
    Long userId = userService.getCurrentUserId();
    User user = userRepository.findById(userId)
      .orElseThrow(() -> new BaseException(UserErrorCode.USER_NOT_FOUND));

    String accessToken = oAuth2TokenClient.ensureAccessToken(user);

    // 가장 최근의 인증 요청(transactionId) 가져오기
    AccountVerificationRequest request = accountVerificationRequestRepository
      .findTopByUser_IdOrderByCreatedAtDesc(userId)
      .orElseThrow(() -> new BaseException(WalletErrorCode.NOT_FOUND_VERIFICATION_REQUEST));

    // 입력 코드로 인증 확인
    VerificationCheckRequestDto checkRequest = new VerificationCheckRequestDto(request.getTransactionId(), inputCode);

    // access token이 없거나 만료되었으면 발급
    VerificationCheckResponseDto checkResponse = hwanbeeAccountClient.verifyInputCode(checkRequest, accessToken);

    VerificationAccountDataDto data = checkResponse.data();
    if (data == null || !Boolean.TRUE.equals(data.verified())) {
      throw new BaseException(WalletErrorCode.FAIL_VERIFICATION);
    }

    // 사용자 및 통화 정보 조회
    userService.getByIdOrThrow(userId);
    Currency currency = currencyService.getByCodeOrThrow(data.currencyCode());

    // 중복된 환비 계좌가 없을 경우 새로 저장
    HwanbeeAccountLink accountLink = hwanbeeLinkRepository
      .findByUserIdAndHwanbeeBankAccountNumber(userId, data.accountNumber())
      .orElseGet(() -> {
        HwanbeeAccountLink link = HwanbeeAccountLink.create(
          userId,
          data.accountNumber(), // 환비 계좌 정보
          data.currencyCode()
        );
        return hwanbeeLinkRepository.save(link);
      });

    return BankAccountResponseDto.from(accountLink);
  }

  /**
   * 연결된 환비 계좌 목록 조회
   */
  @Override
  public List<BankAccountResponseDto> getBankAccountsByUser() {
    // 1) 현재 로그인한 사용자 ID 조회
    Long userId = userService.getCurrentUserId();

    // 2) 저장된 환비 계좌 링크 전체 조회
    List<HwanbeeAccountLink> links = hwanbeeLinkRepository.findByUserId(userId);

    // 3) DTO 변환 후 반환
    return links.stream()
      .map(BankAccountResponseDto::from)
      .collect(Collectors.toList());
  }

  /**
   * 사용자별 통화 코드로 지갑 조회
   */
  @Override
  public SearchWalletResponseDto getWalletByUserAndCurrency(SearchWalletRequestDto req) {
    Long userId = userService.getCurrentUserId();

    Wallet wallet = walletRepository.findByUserIdAndCurrencyCode(userId, req.currencyCode())
      .orElseThrow(() -> BaseException.type(WalletErrorCode.NOT_FOUND_WALLET));

    return SearchWalletResponseDto.from(wallet);
  }

  /**
   * 사용자별 모든 지갑 목록 조회
   */
  @Override
  @Transactional
  public List<SearchWalletResponseDto> getAllWalletsByUser() {
    Long userId = userService.getCurrentUserId();

    return walletRepository.findAllByUser(userId).stream()
      .map(SearchWalletResponseDto::from)
      .collect(Collectors.toList());
  }

  /**
   * 지갑 번호로 지갑 조회
   */
  @Override
  public Wallet getWalletByNumberOrThrow(String walletNumber) {
    return walletRepository.findByWalletNumber(walletNumber)
      .orElseThrow(() -> BaseException.type(WalletErrorCode.NOT_FOUND_WALLET));
  }

  /**
   * 현재 로그인한 사용자의 환비 계좌(은행 계좌) 목록을 조회
   * currencyCode가 null이 아니면 해당 통화만 필터링
   */
  public List<BankAccountResponseDto> getBankAccountsByUser(String currencyCode) {

    // 현재 로그인한 사용자의 ID를 조회합니다.
    Long userId = userService.getCurrentUserId();

    List<HwanbeeAccountLink> links;

    // 통화 코드가 null이 아니고 공백이 아닌 경우 (즉, 특정 통화로 필터링하는 경우)
    if (currencyCode != null && !currencyCode.isBlank()) {
      // 해당 사용자 ID와 통화 코드로 환비 계좌를 조회합니다.
      links = hwanbeeLinkRepository.findByUserIdAndCurrencyCode(userId, currencyCode);
    } else {
      // 통화 코드가 없거나 공백일 경우, 해당 사용자의 전체 환비 계좌를 조회합니다.
      links = hwanbeeLinkRepository.findByUserId(userId);
    }

    // 조회한 계좌 엔티티들을 DTO로 변환하여 반환합니다.
    return links.stream()
      .map(BankAccountResponseDto::from)  // 각 엔티티를 BankAccountResponseDto로 변환
      .collect(Collectors.toList());      // 리스트로 수집하여 반환
  }

  @Override
  public Wallet getWalletByNumberAndVerifyCurrency(String walletNumber, String currencyCode) {
    Wallet wallet = walletRepository.findByWalletNumber(walletNumber)
      .orElseThrow(() -> new BaseException(WalletErrorCode.NOT_FOUND_WALLET));

    if (!wallet.getCurrency().getCode().equals(currencyCode)) {
      throw new BaseException(WalletErrorCode.CURRENCY_MISMATCH);
    }

    return wallet;
  }

  /**
   * 지갑 번호 생성 (예: 1234-5678-9012-3456)
   */
  private String generateWalletNumber() {
    int part1 = (int)(Math.random() * 9000) + 1000;
    int part2 = (int)(Math.random() * 9000) + 1000;
    int part3 = (int)(Math.random() * 9000) + 1000;
    int part4 = (int)(Math.random() * 9000) + 1000;

    return String.format("%04d-%04d-%04d-%04d", part1, part2, part3, part4);
  }
}
