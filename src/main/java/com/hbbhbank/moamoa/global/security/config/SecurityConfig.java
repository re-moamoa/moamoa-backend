package com.hbbhbank.moamoa.global.security.config;

import com.hbbhbank.moamoa.global.constant.AuthConstant;
import com.hbbhbank.moamoa.global.security.handler.CustomAccessDeniedHandler;
import com.hbbhbank.moamoa.global.security.handler.CustomAuthenticationEntryPointHandler;
import com.hbbhbank.moamoa.global.security.handler.CustomLogoutProcessHandler;
import com.hbbhbank.moamoa.global.security.handler.CustomLogoutResultHandler;
import com.hbbhbank.moamoa.global.security.filter.JwtAuthenticationFilter;
import com.hbbhbank.moamoa.global.security.filter.JwtExceptionFilter;
import com.hbbhbank.moamoa.global.security.util.JwtUtil;
import com.hbbhbank.moamoa.global.security.provider.JwtAuthenticationManager;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration // 해당 클래스가 스프링 설정 클래스임을 명시
@EnableWebSecurity // Spring Security를 활성화
@EnableMethodSecurity(prePostEnabled = true) // @PreAuthorize 등 메서드 단위 권한 제어 활성화
@RequiredArgsConstructor // 생성자 주입을 위한 Lombok 어노테이션
public class SecurityConfig {

  // 로그아웃 시 내부 처리 로직을 담당하는 핸들러
  private final CustomLogoutProcessHandler customLogoutProcessHandler;

  // 로그아웃이 성공했을 때 응답을 내려주는 핸들러
  private final CustomLogoutResultHandler customLogoutResultHandler;

  // 인증이 되지 않은 사용자가 접근했을 때 처리할 핸들러
  private final CustomAuthenticationEntryPointHandler customAuthenticationEntryPointHandler;

  // 인가(권한)되지 않은 사용자가 접근했을 때 처리할 핸들러
  private final CustomAccessDeniedHandler customAccessDeniedHandler;

  // JWT 토큰 관련 유틸 클래스 (파싱, 검증 등)
  private final JwtUtil jwtUtil;

  // JWT 기반 인증 처리를 담당하는 커스텀 AuthenticationManager
  private final JwtAuthenticationManager jwtAuthenticationManager;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http
      .cors(Customizer.withDefaults()) // CORS 설정 활성화 (corsConfigurationSource에서 정의된 정책 적용)
      .csrf(AbstractHttpConfigurer::disable) // CSRF 보호 비활성화 (JWT 기반이므로 상태 비저장 방식)
      .httpBasic(AbstractHttpConfigurer::disable) // 기본 브라우저 인증창 비활성화
      .formLogin(AbstractHttpConfigurer::disable) // 스프링 시큐리티 기본 로그인 폼 사용 안 함

      .sessionManagement(sessionManagement ->
        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      // 세션을 생성하지 않고 상태를 저장하지 않는 방식으로 설정 (JWT 기반 인증은 매 요청마다 토큰 검증)

      .authorizeHttpRequests(auth -> auth
        .requestMatchers(AuthConstant.NOT_NEED_AUTH.toArray(String[]::new)).permitAll() // 인증 없이 접근 가능한 URL 목록 허용
        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll() // 정적 리소스(js, css 등) 허용
        .anyRequest().authenticated() // 위의 예외를 제외한 모든 요청은 인증 필요
      )

      .logout(logout -> logout
        .logoutUrl("/api/v1/auth/logout") // 로그아웃 요청을 받을 URL 지정
        .addLogoutHandler(customLogoutProcessHandler) // 로그아웃 처리 핸들러 등록 (RefreshToken 삭제 등)
        .logoutSuccessHandler(customLogoutResultHandler) // 로그아웃 성공 시 결과 반환 핸들러 등록
      )

      .exceptionHandling(exception -> exception
        .authenticationEntryPoint(customAuthenticationEntryPointHandler) // 인증 실패 시 JSON 응답
        .accessDeniedHandler(customAccessDeniedHandler) // 인가 실패 시 JSON 응답
      )

      .addFilterBefore(
        new JwtAuthenticationFilter(jwtUtil, jwtAuthenticationManager), LogoutFilter.class) // JWT 인증 필터를 로그아웃 필터보다 먼저 실행

      .addFilterBefore(
        new JwtExceptionFilter(), JwtAuthenticationFilter.class) // JWT 예외를 처리하는 필터를 인증 필터보다 먼저 실행

      .getOrBuild(); // 설정된 SecurityFilterChain을 생성 및 반환
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(true); // 인증 정보를 포함한 요청 허용 (예: Authorization 헤더)
    config.addAllowedOriginPattern("*"); // 모든 도메인 허용 (개발 환경 전용, 운영 시 특정 도메인만 허용 권장)
    config.addAllowedHeader("*"); // 모든 헤더 허용
    config.addAllowedMethod("*"); // 모든 HTTP 메서드 허용 (GET, POST, PUT 등)

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config); // 모든 경로에 위 설정 적용
    return source; // Spring Security에서 사용할 CORS 정책 반환
  }
}