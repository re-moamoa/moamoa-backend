package com.hbbhbank.moamoa.external.client;

import com.hbbhbank.moamoa.external.auth.OAuth2TokenService;
import com.hbbhbank.moamoa.external.common.HwanbeeApiEndpoints;
import com.hbbhbank.moamoa.external.dto.request.exchange.ExchangeDealRequestDto;
import com.hbbhbank.moamoa.external.dto.response.exchange.ExchangeDealResponseDto;
import com.hbbhbank.moamoa.external.dto.response.exchange.ExchangeRateResponseDto;
import com.hbbhbank.moamoa.external.dto.response.exchange.SingleExchangeRateResponseDto;
import com.hbbhbank.moamoa.global.exception.BaseException;
import com.hbbhbank.moamoa.global.security.util.SecurityUtil;
import com.hbbhbank.moamoa.user.domain.User;
import com.hbbhbank.moamoa.user.exception.UserErrorCode;
import com.hbbhbank.moamoa.user.repository.UserRepository;
import org.springframework.cache.annotation.Cacheable;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class HwanbeeExchangeClientImpl implements HwanbeeExchangeClient {

  private final RestTemplate hwanbeeRestTemplate;
  private final HwanbeeApiEndpoints hwanbeeApiEndpoints;
  private final OAuth2TokenService oAuth2TokenService;
  private final UserRepository userRepository;

  @Override
  public ExchangeRateResponseDto getAllExchangeRatesV1() {
    String accessToken = ensureValidAccessToken();
    String url = hwanbeeApiEndpoints.getExchangeRatesUrl();
    log.info("[외부 호출] 모든 환율 조회 API (캐시 미사용): {}", url);

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);
    HttpEntity<Void> request = new HttpEntity<>(headers);

    ResponseEntity<ExchangeRateResponseDto> response = hwanbeeRestTemplate.exchange(
      url,
      HttpMethod.GET,
      request,
      ExchangeRateResponseDto.class
    );

    log.info("[응답 수신] 모든 환율 - 상태 (캐시 미사용): {}", response.getStatusCode());
    return response.getBody();
  }

  @Cacheable(cacheResolver="redisCacheResolver", value = "redisExchangeRates", key = "'dailyRates'")
  @Override
  public ExchangeRateResponseDto getAllExchangeRatesV2() {
    String accessToken = ensureValidAccessToken();
    String url = hwanbeeApiEndpoints.getExchangeRatesUrl();
    log.info("[외부 호출] 모든 환율 조회 API (Redis 캐시 사용): {}", url);

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);
    HttpEntity<Void> request = new HttpEntity<>(headers);

    ResponseEntity<ExchangeRateResponseDto> response = hwanbeeRestTemplate.exchange(
      url,
      HttpMethod.GET,
      request,
      ExchangeRateResponseDto.class
    );

    log.info("[응답 수신] 모든 환율 - 상태 (Redis 캐시 사용): {}", response.getStatusCode());
    return response.getBody();
  }

  @Cacheable(value = "memoryExchangeRates", key = "'dailyRates'")
  @Override
  public ExchangeRateResponseDto getAllExchangeRatesV3() {
    String accessToken = ensureValidAccessToken();
    String url = hwanbeeApiEndpoints.getExchangeRatesUrl();
    log.info("[외부 호출] 모든 환율 조회 API (인메모리 캐시 사용): {}", url);

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);
    HttpEntity<Void> request = new HttpEntity<>(headers);

    ResponseEntity<ExchangeRateResponseDto> response = hwanbeeRestTemplate.exchange(
      url,
      HttpMethod.GET,
      request,
      ExchangeRateResponseDto.class
    );

    log.info("[응답 수신] 모든 환율 - 상태 (인메모리 캐시 사용): {}", response.getStatusCode());
    return response.getBody();
  }

  @Override
  public SingleExchangeRateResponseDto getExchangeRateByCurrency(String accessToken, String currencyCode) {
    String url = UriComponentsBuilder
      .fromHttpUrl(hwanbeeApiEndpoints.getExchangeRateUrl())
      .queryParam("currency", currencyCode)
      .toUriString();

    log.info("[외부 호출] 단일 환율 조회 API: {}", url);

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);
    HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

    ResponseEntity<SingleExchangeRateResponseDto> response = hwanbeeRestTemplate.exchange(
      url,
      HttpMethod.GET,
      requestEntity,
      SingleExchangeRateResponseDto.class
    );

    log.info("[응답 수신] 단일 환율 - 통화: {} / 상태: {}", currencyCode, response.getStatusCode());
    return response.getBody();
  }

  @Override
  public ExchangeDealResponseDto requestExchange(String accessToken, ExchangeDealRequestDto request) {
    String url = hwanbeeApiEndpoints.getExchangeDealUrl();
    log.debug("[외부 호출] 환전 실행 API: {}", url);

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);
    headers.setContentType(MediaType.APPLICATION_JSON);

    HttpEntity<ExchangeDealRequestDto> requestEntity = new HttpEntity<>(request, headers);

    ResponseEntity<ExchangeDealResponseDto> response = hwanbeeRestTemplate.exchange(
      url,
      HttpMethod.POST,
      requestEntity,
      ExchangeDealResponseDto.class
    );

    log.info("[응답 수신] 환전 완료 - KRW계좌: {} → FCY계좌: {}, 원화금액: {}, 통화코드: {}, HTTP 상태: {}",
      request.krwAccount(),
      request.fcyAccount(),
      request.krwAmount(),
      request.currencyCode(),
      response.getStatusCode()
    );

    return response.getBody();
  }

  private String ensureValidAccessToken() {
    Long userId = SecurityUtil.getCurrentUserId();
    User user = userRepository.findById(userId)
      .orElseThrow(() -> new BaseException(UserErrorCode.USER_NOT_FOUND));
    return oAuth2TokenService.ensureAccessToken(user);
  }
}
