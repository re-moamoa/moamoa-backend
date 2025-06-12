package com.hbbhbank.moamoa.wallet.controller;

import com.hbbhbank.moamoa.global.common.BaseResponse;
import com.hbbhbank.moamoa.user.service.UserService;
import com.hbbhbank.moamoa.wallet.dto.request.transaction.TransactionFilterRequestDto;
import com.hbbhbank.moamoa.wallet.dto.response.transaction.TransactionResponseDto;
import com.hbbhbank.moamoa.wallet.service.WalletTransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class WalletTransactionController {

  private final WalletTransactionService walletTransactionService;
  private final UserService userService;

  @GetMapping
  public ResponseEntity<BaseResponse<Page<TransactionResponseDto>>> getAllTransactions(
    @Valid TransactionFilterRequestDto req
  ) {
    Long userId = userService.getCurrentUserId(); // 로그인 사용자 ID
    return ResponseEntity.ok(BaseResponse.success(walletTransactionService.findAll(req, userId)));
  }

  @GetMapping("/latest")
  public ResponseEntity<BaseResponse<TransactionResponseDto>> getLatestTransaction() {
    Long userId = userService.getCurrentUserId();
    return ResponseEntity.ok(BaseResponse.success(walletTransactionService.findLatest(userId)));
  }
}
