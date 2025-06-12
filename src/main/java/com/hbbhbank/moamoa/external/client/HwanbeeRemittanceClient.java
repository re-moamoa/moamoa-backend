package com.hbbhbank.moamoa.external.client;

import com.hbbhbank.moamoa.external.dto.request.transfer.HwanbeeRemittanceRequestDto;
import com.hbbhbank.moamoa.external.dto.response.transfer.HwanbeeRemittanceResponseDto;

public interface HwanbeeRemittanceClient {
  HwanbeeRemittanceResponseDto remitFromUserAccount(HwanbeeRemittanceRequestDto req, String accessToken);
}
