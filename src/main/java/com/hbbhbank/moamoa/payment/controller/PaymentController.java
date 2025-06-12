package com.hbbhbank.moamoa.payment.controller;

import com.hbbhbank.moamoa.global.common.BaseResponse;
import com.hbbhbank.moamoa.global.security.principal.UserPrincipal;
import com.hbbhbank.moamoa.payment.dto.request.PaymentRequestDto;
import com.hbbhbank.moamoa.payment.dto.response.QrCodeCreateResponseDto;
import com.hbbhbank.moamoa.payment.dto.response.QrCodeInfoResponseDto;
import com.hbbhbank.moamoa.payment.service.PaymentServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

  private final PaymentServiceImpl paymentService;

  /**
   * 판매자가 QR 코드를 생성
   */
  @PostMapping("/{walletId}/qr-code")
  public ResponseEntity<BaseResponse<QrCodeCreateResponseDto>> generateQrCode(
    @PathVariable final Long walletId
  ) {
    return ResponseEntity.ok(
      BaseResponse.success(paymentService.generateQr(walletId))
    );
  }

  /**
   * QR 코드 이미지 조회
   */
  @GetMapping("/qr-code-images/{qrId}")
  public ResponseEntity<byte[]> getQrCodeImage(@PathVariable Long qrId) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.IMAGE_PNG);
    return ResponseEntity.ok().headers(headers).body(
      paymentService.getQRCodeImage(qrId)
    );
  }

  /**
   * 구매자가 QR 코드를 스캔하고 결제 진행
   */
  @PostMapping("/use/{qrUuid}")
  public ResponseEntity<BaseResponse<Void>> useQrCode(
    @PathVariable String qrUuid,
    @RequestBody @Valid PaymentRequestDto req
  ) {

    paymentService.payWithQr(qrUuid, req);
    return ResponseEntity.ok(BaseResponse.success(null));
  }

  /**
   * QR 코드 재발급
   */
  @PostMapping("/{walletId}/qr-code/reissue")
  public ResponseEntity<BaseResponse<QrCodeCreateResponseDto>> reissueQrCode(
    @PathVariable final Long walletId
  ) {
    return ResponseEntity.ok(
      BaseResponse.success(paymentService.reissueQr(walletId))
    );
  }

  @GetMapping("/qr-code/{uuid}")
  public ResponseEntity<BaseResponse<QrCodeInfoResponseDto>> getQrInfo(@PathVariable String uuid) {
    return ResponseEntity.ok(BaseResponse.success(paymentService.getQrInfo(uuid)));
  }

}
