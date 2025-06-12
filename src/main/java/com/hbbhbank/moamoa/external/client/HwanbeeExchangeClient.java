package com.hbbhbank.moamoa.external.client;

import com.hbbhbank.moamoa.external.dto.request.exchange.ExchangeDealRequestDto;
import com.hbbhbank.moamoa.external.dto.response.exchange.ExchangeDealResponseDto;
import com.hbbhbank.moamoa.external.dto.response.exchange.ExchangeRateResponseDto;
import com.hbbhbank.moamoa.external.dto.response.exchange.SingleExchangeRateResponseDto;

public interface HwanbeeExchangeClient {

  /**
   * 모든 환율 정보를 조회합니다. (캐시 미사용)
   */
  ExchangeRateResponseDto getAllExchangeRatesV1();

  /**
   * 모든 환율 정보를 조회합니다. (Redis 캐시 사용)
   */
  ExchangeRateResponseDto getAllExchangeRatesV2();

  /**
   * 모든 환율 정보를 조회합니다. (인메모리 캐시 사용)
   */
  ExchangeRateResponseDto getAllExchangeRatesV3();

  /**
   * 특정 통화의 환율 정보를 조회합니다.
   */
  SingleExchangeRateResponseDto getExchangeRateByCurrency(String accessToken, String currencyCode);

  /**
   * 환전 요청을 처리합니다.
   */
  ExchangeDealResponseDto requestExchange(String accessToken, ExchangeDealRequestDto request);

}
