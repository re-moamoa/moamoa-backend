package com.hbbhbank.moamoa.exchange.controller;

import com.hbbhbank.moamoa.external.client.HwanbeeExchangeClient;
import com.hbbhbank.moamoa.external.dto.request.exchange.ExchangeDealRequestDto;
import com.hbbhbank.moamoa.external.dto.response.exchange.ExchangeDealResponseDto;
import com.hbbhbank.moamoa.external.dto.response.exchange.ExchangeRateResponseDto;
import com.hbbhbank.moamoa.external.dto.response.exchange.SingleExchangeRateResponseDto;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 통합 테스트: v1, v2, v3 엔드포인트를 각각 호출하여 응답 시간을 측정하고 비교
 *
 * - HwanbeeExchangeClient를 내부 TestConfiguration으로 교체하여, 외부 호출 없이 더미 응답을 반환하도록 설정
 * - 보안 설정을 무시하고 모든 요청을 허용하도록 SecurityFilterChain을 덮어씀
 * - CacheManager가 Redis/인메모리 둘 다 빈으로 등록되어 있으므로, @Primary로 메인 캐시매니저를 지정
 */
@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  properties = "spring.main.allow-bean-definition-overriding=true"
)
@ActiveProfiles("default")
@Import({
  ExchangeRateIntegrationTest.TestSecurityConfig.class,
  ExchangeRateIntegrationTest.PrimaryCacheManagerConfig.class,
  ExchangeRateIntegrationTest.DummyHwanbeeClientConfig.class
})
public class ExchangeRateIntegrationTest {

  @Resource
  private TestRestTemplate restTemplate;

  @Resource
  private CacheManager cacheManager;

  // 테스트용 더미 응답 DTO
  private ExchangeRateResponseDto dummyDto;

  @BeforeEach
  void setUp() {
    // Redis + Memory 캐시 모두 초기화
    Cache redisCache = cacheManager.getCache("redisExchangeRates");
    if (redisCache != null) {
      redisCache.clear();
    }
    Cache memoryCache = cacheManager.getCache("memoryExchangeRates");
    if (memoryCache != null) {
      memoryCache.clear();
    }

    // 빈 리스트 형태의 더미 DTO 생성 (status=200, message="OK", data=빈 리스트)
    dummyDto = new ExchangeRateResponseDto(200, "OK", Collections.emptyList());
  }

  @Test
  void measureResponseTimesForV1V2V3() {
    long start, end;

    // 1. v1 (캐시 미사용) 호출
    start = System.nanoTime();
    ResponseEntity<String> v1Response = restTemplate.getForEntity("/api/v1/exchange/rates-v1", String.class);
    end = System.nanoTime();
    long v1TimeMillis = TimeUnit.NANOSECONDS.toMillis(end - start);
    assertThat(v1Response.getStatusCode().is2xxSuccessful()).isTrue();

    // 2. v2 (Redis 캐시) 첫 호출 – 캐시가 비어 있으므로 dummyClient를 통해 dummyDto 반환
    start = System.nanoTime();
    ResponseEntity<String> v2FirstResponse = restTemplate.getForEntity("/api/v1/exchange/rates-v2", String.class);
    end = System.nanoTime();
    long v2FirstTimeMillis = TimeUnit.NANOSECONDS.toMillis(end - start);
    assertThat(v2FirstResponse.getStatusCode().is2xxSuccessful()).isTrue();

    // 3. v2 (Redis 캐시) 두 번째 호출 – 캐시 히트, 더 빠름
    start = System.nanoTime();
    ResponseEntity<String> v2SecondResponse = restTemplate.getForEntity("/api/v1/exchange/rates-v2", String.class);
    end = System.nanoTime();
    long v2SecondTimeMillis = TimeUnit.NANOSECONDS.toMillis(end - start);
    assertThat(v2SecondResponse.getStatusCode().is2xxSuccessful()).isTrue();

    // 4. v3 (인메모리 캐시) 첫 호출 – 캐시가 비어 있으므로 dummyClient를 통해 dummyDto 반환
    start = System.nanoTime();
    ResponseEntity<String> v3FirstResponse = restTemplate.getForEntity("/api/v1/exchange/rates-v3", String.class);
    end = System.nanoTime();
    long v3FirstTimeMillis = TimeUnit.NANOSECONDS.toMillis(end - start);
    assertThat(v3FirstResponse.getStatusCode().is2xxSuccessful()).isTrue();

    // 5. v3 (인메모리 캐시) 두 번째 호출 – 캐시 히트, 더 빠름
    start = System.nanoTime();
    ResponseEntity<String> v3SecondResponse = restTemplate.getForEntity("/api/v1/exchange/rates-v3", String.class);
    end = System.nanoTime();
    long v3SecondTimeMillis = TimeUnit.NANOSECONDS.toMillis(end - start);
    assertThat(v3SecondResponse.getStatusCode().is2xxSuccessful()).isTrue();

    // 콘솔에 결과 로그 출력
    System.out.printf(
      "Response Times (ms): v1 = %d | v2-first = %d | v2-second = %d | v3-first = %d | v3-second = %d%n",
      v1TimeMillis, v2FirstTimeMillis, v2SecondTimeMillis, v3FirstTimeMillis, v3SecondTimeMillis
    );
  }

  /**
   * RedisCacheManager와 MemoryCacheManager가 둘 다 빈으로 등록되어 발생하는
   * NoUniqueBeanDefinitionException을 방지하기 위해, MemoryCacheManager를 @Primary로 지정합니다.
   */
  @TestConfiguration
  static class PrimaryCacheManagerConfig {
    @Bean
    @Primary
    // 테스트할 때, 무조건! InMemoryCacheConfig에 있는 @Primary 주석처리 해주기.
    public CacheManager primaryCacheManager(CacheManager memoryCacheManager) {
      return memoryCacheManager;
    }
  }

  /**
   * 테스트 시 보안 설정을 무시하고 모든 요청을 허용하도록 SecurityFilterChain을 교체합니다.
   * spring.main.allow-bean-definition-overriding=true 덕분에,
   * 기존 SecurityConfig의 filterChain 빈을 이쪽에서 정의한 빈이 덮어씁니다.
   */
  @TestConfiguration
  static class TestSecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
      http
        .authorizeHttpRequests(authz -> authz.anyRequest().permitAll())
        .csrf(csrf -> csrf.disable());
      return http.build();
    }
  }

  /**
   * 실제 HwanbeeExchangeClient 대신, 더미 구현을 빈으로 등록합니다.
   * 이 빈이 기존 구현을 @Primary로 덮어쓰기 때문에, 컨트롤러/서비스 로직에서 호출 시
   * 항상 같은 dummyDto(200, "OK", emptyList)만 반환됩니다.
   */
  @TestConfiguration
  static class DummyHwanbeeClientConfig {

    @Bean
    @Primary
    public HwanbeeExchangeClient dummyHwanbeeExchangeClient() {
      return new HwanbeeExchangeClient() {
        @Override
        public ExchangeRateResponseDto getAllExchangeRatesV1() {
          // 항상 동일한 더미 응답 반환
          return new ExchangeRateResponseDto(200, "OK", Collections.emptyList());
        }

        @Override
        public ExchangeRateResponseDto getAllExchangeRatesV2() {
          return new ExchangeRateResponseDto(200, "OK", Collections.emptyList());
        }

        @Override
        public ExchangeRateResponseDto getAllExchangeRatesV3() {
          return new ExchangeRateResponseDto(200, "OK", Collections.emptyList());
        }

        // 테스트 안함
        @Override
        public SingleExchangeRateResponseDto getExchangeRateByCurrency(String accessToken, String currencyCode) {
          return null;
        }

        // 테스트 안함
        @Override
        public ExchangeDealResponseDto requestExchange(String accessToken, ExchangeDealRequestDto request) {
          return null;
        }
      };
    }
  }
}