package com.hbbhbank.moamoa.external.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

  /**
   * 환비 API 호출용 RestTemplate.
   * 사용자별 access token은 코드에서 직접 주입하므로 인터셉터는 필요하지 않음.
   */
  @Bean(name = "hwanbeeRestTemplate")
  public RestTemplate hwanbeeRestTemplate() {
    return new RestTemplate();
  }

  /**
   * 토큰 교환용 RestTemplate (Bearer 토큰 없음, Basic 인증만 필요).
   */
  @Bean(name = "tokenRestTemplate")
  public RestTemplate tokenRestTemplate() {
    return new RestTemplate();
  }
}
