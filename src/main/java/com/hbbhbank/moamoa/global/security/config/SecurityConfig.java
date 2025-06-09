package com.hbbhbank.moamoa.global.security.config;

import com.hbbhbank.moamoa.global.constant.Constants;
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
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
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
      .cors(Customizer.withDefaults()) // CORS 설정 활성화
      .csrf(AbstractHttpConfigurer::disable) // CSRF 보호 비활성화 (JWT 기반이므로 상태 비저장 방식)
      .httpBasic(AbstractHttpConfigurer::disable) // 기본 브라우저 인증창 비활성화
      .formLogin(AbstractHttpConfigurer::disable) // 스프링 시큐리티 기본 로그인 폼 사용 안 함

      // 세션을 생성하지 않고 상태를 저장하지 않는 방식으로 설정 (JWT 기반 인증은 매 요청마다 토큰 검증)
      .sessionManagement(sessionManagement ->
        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

      .authorizeHttpRequests(auth -> auth
        .requestMatchers(Constants.NOT_NEED_AUTH.toArray(String[]::new)).permitAll() // 인증 없이 접근 가능한 URL 목록 허용
        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll() // 정적 리소스(js, css 등) 허용
        .requestMatchers("/api/v1/**").hasAnyRole("USER") // ROLE_USER 권한을 가진 사용자만 접근 가능
        .anyRequest().authenticated() // 위의 예외를 제외한 모든 요청은 인증 필요
      )

      .logout(logout -> logout
        .logoutUrl("/api/v1/auth/logout") // 로그아웃 요청을 받을 URL 지정
        .addLogoutHandler(customLogoutProcessHandler) // 로그아웃 처리 핸들러 등록 (RefreshToken 삭제 등)
        .logoutSuccessHandler(customLogoutResultHandler) // 로그아웃 성공 시 결과 반환 핸들러 등록
      )

      .exceptionHandling(exception -> exception
        .accessDeniedHandler(customAccessDeniedHandler) // 인가 실패 시 JSON 응답
        .authenticationEntryPoint(customAuthenticationEntryPointHandler) // 인증 실패 시 JSON 응답
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
    config.setAllowCredentials(true); // 인증 포함 요청 허용

    // 운영 환경과 개발 환경 각각 명시
    config.setAllowedOrigins(List.of(
      "https://moamoa-front.vercel.app",
      "http://localhost:3000"
    ));

    config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")); // 명시적 메서드 허용

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }


  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}