package com.hbbhbank.moamoa.wallet.controller;

import com.hbbhbank.moamoa.external.dto.request.account.VerificationCodeRequestDto;
import com.hbbhbank.moamoa.global.common.BaseResponse;
import com.hbbhbank.moamoa.wallet.domain.Wallet;
import com.hbbhbank.moamoa.wallet.dto.request.wallet.CreateWalletRequestDto;
import com.hbbhbank.moamoa.wallet.dto.request.wallet.SearchWalletRequestDto;
import com.hbbhbank.moamoa.wallet.dto.response.wallet.BankAccountResponseDto;
import com.hbbhbank.moamoa.wallet.dto.response.wallet.CreateWalletResponseDto;
import com.hbbhbank.moamoa.wallet.dto.response.wallet.SearchWalletResponseDto;
import com.hbbhbank.moamoa.wallet.service.WalletServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wallet")
@RequiredArgsConstructor
@Validated
public class WalletController {

  private final WalletServiceImpl walletService;

  /**
   * 환비 계좌번호를 연결하기 위해 1회용 인증코드를 요청합니다.
   * 인증 코드는 환비 API 앱에서 확인할 수 있으며,
   * 응답 DTO에 포함된 transactionId는 추후 코드 확인 시 필요합니다.
   *
   * @param requestDto 환비 계좌번호의 정보를 담은 요청 DTO
   * @return 인증코드 발급 결과를 담은 응답 DTO
   */
  @PostMapping("/verification-code")
  public ResponseEntity<Void> generateVerificationCode(
    @RequestBody @Valid VerificationCodeRequestDto requestDto
  ) {
    walletService.requestVerificationCode(requestDto);
    return ResponseEntity.noContent().build();
  }

  /**
   * 인증 코드 검증 후 환비 계좌와 연결된 지갑을 생성합니다.
   * 지갑은 통화 코드 및 외부 계좌 정보에 따라 하나만 생성 가능하며, 중복 생성은 불가능합니다.
   *
   * @param requestDto 인증코드 등의 정보를 담은 요청 DTO
   * @return 지갑 id, 사용자 이름, 지갑 번호, 통화 코드, 통화 이름, 잔액, 환비 계좌번호
   */
  @PostMapping
  public ResponseEntity<BaseResponse<CreateWalletResponseDto>> createWallet(
    @RequestBody @Valid CreateWalletRequestDto requestDto
  ) {
    CreateWalletResponseDto response = walletService.createWalletAfterVerification(requestDto.inputCode());
    return ResponseEntity.ok(BaseResponse.success(response));
  }

  /**
   * 인증 코드 검증 후, 환전을 위해 환비 계좌와 연결합니다.
   *
   * @param inputCode 인증 코드 (환비 앱에서 확인 가능)
   * @return VerificationCheckResponseDto
   */
  @GetMapping("/hwanbee-account")
  public ResponseEntity<BaseResponse<BankAccountResponseDto>> searchHwanbeeAccount(
    @RequestBody @Valid String inputCode
  ) {
    BankAccountResponseDto response = walletService.searchHwannbeeAccount(inputCode);
    return ResponseEntity.ok(BaseResponse.success(response));
  }

  /**
   * 현재 로그인한 사용자의 특정 통화에 해당하는 지갑 정보를 조회합니다.
   *
   * @param currencyCode 통화 코드 (예: KRW, USD)
   * @return 지갑 번호, 통화 코드, 통화 이름, 잔액, 환비 계좌번호
   */
  @GetMapping
  public ResponseEntity<BaseResponse<SearchWalletResponseDto>> showWallet(
    @RequestParam String currencyCode
  ) {
    SearchWalletRequestDto dto = new SearchWalletRequestDto(currencyCode);
    return ResponseEntity.ok(BaseResponse.success(
      walletService.getWalletByUserAndCurrency(dto))
    );
  }

  /**
   * 현재 로그인한 사용자의 모든 가상 지갑 목록을 조회합니다.
   *
   * @return 통화별 지갑 정보 리스트
   */
  @GetMapping("/all")
  public ResponseEntity<BaseResponse<List<SearchWalletResponseDto>>> getAllWallets() {
    return ResponseEntity.ok(BaseResponse.success(
      walletService.getAllWalletsByUser())
    );
  }

  /**
   * 현재 로그인한 사용자의 환비 계좌를 통화 코드를 통해 조회합니다.
   * @param currencyCode
   * @return
   */
  @GetMapping("/bank-accounts")
  public ResponseEntity<BaseResponse<List<BankAccountResponseDto>>> getBankAccounts(
    @RequestParam(required = false) String currencyCode
  ) {
    List<BankAccountResponseDto> accounts = walletService.getBankAccountsByUser(currencyCode);
    return ResponseEntity.ok(BaseResponse.success(accounts));
  }

  /**
   * 해당 통화 코드의 모든 지갑을 사용자 상관 없이 존재
   * @param currencyCode
   * @return
   */
  @GetMapping("/search")
  public ResponseEntity<BaseResponse<SearchWalletResponseDto>> getWalletByCurrencyAndNumber(
    @RequestParam String walletNumber,
    @RequestParam String currencyCode
  ) {
    Wallet wallet = walletService.getWalletByNumberAndVerifyCurrency(walletNumber, currencyCode);
    return ResponseEntity.ok(BaseResponse.success(SearchWalletResponseDto.from(wallet)));
  }

  // TODO: 환비의 원화 계좌 잔액 조회

  // TODO: 환비의 외화 계좌 잔액 조회
}

