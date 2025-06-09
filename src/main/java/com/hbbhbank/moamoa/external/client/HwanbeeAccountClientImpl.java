package com.hbbhbank.moamoa.external.client;

import com.hbbhbank.moamoa.external.common.HwanbeeApiEndpoints;
import com.hbbhbank.moamoa.external.dto.request.account.VerificationCheckRequestDto;
import com.hbbhbank.moamoa.external.dto.request.account.VerificationCodeRequestDto;
import com.hbbhbank.moamoa.external.dto.response.account.VerificationCheckResponseDto;
import com.hbbhbank.moamoa.external.dto.response.account.VerificationCodeResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class HwanbeeAccountClientImpl implements HwanbeeAccountClient{

  private final RestTemplate hwanbeeRestTemplate;
  private final HwanbeeApiEndpoints hwanbeeApiEndpoints;

  @Override
  public VerificationCodeResponseDto requestVerificationCode(VerificationCodeRequestDto req, String accessToken) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<VerificationCodeRequestDto> request = new HttpEntity<>(req, headers);

    ResponseEntity<VerificationCodeResponseDto> response = hwanbeeRestTemplate.exchange(
      hwanbeeApiEndpoints.getVerificationCodeUrl(),
      HttpMethod.POST,
      request,
      VerificationCodeResponseDto.class
    );

    return response.getBody();
  }

  @Override
  public VerificationCheckResponseDto verifyInputCode(VerificationCheckRequestDto req, String accessToken) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<VerificationCheckRequestDto> request = new HttpEntity<>(req, headers);

    ResponseEntity<VerificationCheckResponseDto> response = hwanbeeRestTemplate.exchange(
      hwanbeeApiEndpoints.getVerificationCheckUrl(),
      HttpMethod.POST,
      request,
      VerificationCheckResponseDto.class
    );

    return response.getBody();
  }
}
