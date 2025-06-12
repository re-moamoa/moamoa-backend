package com.hbbhbank.moamoa.exchange.service;

import com.hbbhbank.moamoa.external.client.HwanbeeExchangeClient;
import com.hbbhbank.moamoa.external.dto.request.exchange.ExchangeDealRequestDto;
import com.hbbhbank.moamoa.external.dto.response.exchange.ExchangeDealResponseDto;
import com.hbbhbank.moamoa.external.dto.response.exchange.ExchangeRateResponseDto;
import com.hbbhbank.moamoa.external.dto.response.exchange.SingleExchangeRateResponseDto;
import com.hbbhbank.moamoa.user.domain.User;
import com.hbbhbank.moamoa.user.repository.UserRepository;
import com.hbbhbank.moamoa.global.exception.BaseException;
import com.hbbhbank.moamoa.global.security.util.SecurityUtil;
import com.hbbhbank.moamoa.user.exception.UserErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeServiceImpl implements ExchangeService {

  private final HwanbeeExchangeClient hwanbeeExchangeClient;
  private final UserRepository userRepository;

  @Override
  public ExchangeRateResponseDto getAllExchangeRatesV1() {
    log.info("[환율 조회] 모든 환율 요청 실행 (캐시 미사용)");
    return hwanbeeExchangeClient.getAllExchangeRatesV1();
  }

  @Override
  public ExchangeRateResponseDto getAllExchangeRatesV2() {
    log.info("[환율 조회] 모든 환율 요청 실행 (Redis 캐시 사용)");
    return hwanbeeExchangeClient.getAllExchangeRatesV2();
  }

  @Override
  public ExchangeRateResponseDto getAllExchangeRatesV3() {
    log.info("[환율 조회] 모든 환율 요청 실행 (인메모리 캐시 사용)");
    return hwanbeeExchangeClient.getAllExchangeRatesV3();
  }

  @Override
  public SingleExchangeRateResponseDto getExchangeRateByCurrency(String currencyCode) {
    String accessToken = getAccessTokenForCurrentUser();
    log.info("[환율 조회] 단일 환율 요청 실행 - currencyCode: {}", currencyCode);
    return hwanbeeExchangeClient.getExchangeRateByCurrency(accessToken, currencyCode);
  }

  @Override
  public ExchangeDealResponseDto requestExchange(ExchangeDealRequestDto request) {
    String accessToken = getAccessTokenForCurrentUser();
    return hwanbeeExchangeClient.requestExchange(accessToken, request);
  }

  private String getAccessTokenForCurrentUser() {
    Long userId = SecurityUtil.getCurrentUserId();
    User user = userRepository.findById(userId)
      .orElseThrow(() -> {
        log.warn("[액세스 토큰 조회 실패] 유저 ID: {} → USER_NOT_FOUND", userId);
        return new BaseException(UserErrorCode.USER_NOT_FOUND);
      });

    String accessToken = user.getAccessToken();
    if (accessToken == null || accessToken.isBlank()) {
      log.warn("[액세스 토큰 없음] 유저 ID: {}", userId);
      throw new BaseException(UserErrorCode.HWANBEE_TOKEN_NOT_FOUND);
    }

    log.debug("[액세스 토큰 조회 성공] 유저 ID: {}", userId);
    return accessToken;
  }
}

