package com.hbbhbank.moamoa.external.client;

import com.hbbhbank.moamoa.external.common.HwanbeeApiEndpoints;
import com.hbbhbank.moamoa.external.dto.request.transfer.HwanbeeRemittanceRequestDto;
import com.hbbhbank.moamoa.external.dto.response.transfer.HwanbeeRemittanceResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class HwanbeeRemittanceClientImpl implements HwanbeeRemittanceClient {

  private final RestTemplate hwanbeeRestTemplate;
  private final HwanbeeApiEndpoints hwanbeeApiEndpoints;

  @Override
  public HwanbeeRemittanceResponseDto remitFromUserAccount(HwanbeeRemittanceRequestDto req, String accessToken) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);
    headers.setContentType(MediaType.APPLICATION_JSON);

    HttpEntity<HwanbeeRemittanceRequestDto> request = new HttpEntity<>(req, headers);

    ResponseEntity<HwanbeeRemittanceResponseDto> response = hwanbeeRestTemplate.exchange(
      hwanbeeApiEndpoints.getRemittanceUrl(),
      HttpMethod.POST,
      request,
      HwanbeeRemittanceResponseDto.class
    );

    return response.getBody();
  }
}
