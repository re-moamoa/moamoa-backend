package com.hbbhbank.moamoa.exchange.controller;

import com.hbbhbank.moamoa.exchange.service.ExchangeService;
import com.hbbhbank.moamoa.external.dto.request.exchange.ExchangeDealRequestDto;
import com.hbbhbank.moamoa.external.dto.response.exchange.ExchangeDealResponseDto;
import com.hbbhbank.moamoa.external.dto.response.exchange.ExchangeRateResponseDto;
import com.hbbhbank.moamoa.external.dto.response.exchange.SingleExchangeRateResponseDto;
import com.hbbhbank.moamoa.global.common.BaseResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/exchange")
@RequiredArgsConstructor
public class ExchangeController {

  private final ExchangeService exchangeService;

  /**
   * 전체 환율 정보 조회 (캐시 미사용 버전)
   */
  @GetMapping("/rates-v1")
  public ResponseEntity<BaseResponse<ExchangeRateResponseDto>> getAllRatesV1() {
    ExchangeRateResponseDto dto = exchangeService.getAllExchangeRatesV1();
    log.info("최종 반환(캐시 미사용): status={}, dataSize={}", dto.status(), dto.data() != null ? dto.data().size() : -1);
    return ResponseEntity.ok(BaseResponse.success(dto));
  }

  /**
   * 전체 환율 정보 조회 (Redis 캐시 버전)
   */
  @GetMapping("/rates-v2")
  public ResponseEntity<BaseResponse<ExchangeRateResponseDto>> getAllRatesV2() {
    ExchangeRateResponseDto dto = exchangeService.getAllExchangeRatesV2();
    log.info("최종 반환(Redis 캐시 사용): status={}, dataSize={}", dto.status(), dto.data() != null ? dto.data().size() : -1);
    return ResponseEntity.ok(BaseResponse.success(dto));
  }

  /**
   * 전체 환율 정보 조회 (인메모리 캐시 버전)
   */
  @GetMapping("/rates-v3")
  public ResponseEntity<BaseResponse<ExchangeRateResponseDto>> getAllRatesV3() {
    ExchangeRateResponseDto dto = exchangeService.getAllExchangeRatesV3();
    log.info("최종 반환(인메모리 캐시 사용): status={}, dataSize={}", dto.status(), dto.data() != null ? dto.data().size() : -1);
    return ResponseEntity.ok(BaseResponse.success(dto));
  }

  /**
   * 특정 통화의 환율 정보 조회
   */
  @GetMapping(value = "/rates", params = "currency")
  public ResponseEntity<BaseResponse<SingleExchangeRateResponseDto>> getRateByCurrency(
    @RequestParam String currency
  ) {
    return ResponseEntity.ok(BaseResponse.success(exchangeService.getExchangeRateByCurrency(currency)));
  }

  /**
   * 환전 요청 처리
   */
  @PostMapping("/deal")
  public ResponseEntity<BaseResponse<ExchangeDealResponseDto>> requestExchange(
    @RequestBody ExchangeDealRequestDto requestDto
  ) {
    return ResponseEntity.ok(BaseResponse.success(exchangeService.requestExchange(requestDto)));
  }
}

