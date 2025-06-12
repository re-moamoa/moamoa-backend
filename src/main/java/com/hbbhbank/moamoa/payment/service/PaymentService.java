package com.hbbhbank.moamoa.payment.service;

import com.hbbhbank.moamoa.payment.dto.request.PaymentRequestDto;
import com.hbbhbank.moamoa.payment.dto.response.QrCodeCreateResponseDto;
import com.hbbhbank.moamoa.payment.dto.response.QrCodeInfoResponseDto;

public interface PaymentService {

  QrCodeCreateResponseDto generateQr(Long walletId);

  void payWithQr(String uuid, PaymentRequestDto req);

  byte[] getQRCodeImage(Long qrId);

  QrCodeCreateResponseDto reissueQr(Long walletId);

  QrCodeInfoResponseDto getQrInfo(String uuid);
}
