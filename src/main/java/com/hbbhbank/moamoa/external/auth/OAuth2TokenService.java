package com.hbbhbank.moamoa.external.auth;

import com.hbbhbank.moamoa.external.exception.HwanbeeErrorCode;
import com.hbbhbank.moamoa.global.exception.BaseException;
import com.hbbhbank.moamoa.user.domain.User;
import com.hbbhbank.moamoa.user.exception.UserErrorCode;
import com.hbbhbank.moamoa.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class OAuth2TokenService {

  @Qualifier("tokenRestTemplate")
  private final RestTemplate tokenRestTemplate;

  private final UserRepository userRepository;

  @Value("${oauth2.client-id}")
  private String clientId;

  @Value("${oauth2.client-secret}")
  private String clientSecret;

  @Value("${oauth2.token-uri}")
  private String tokenUri;

  @Value("${oauth2.redirect-uri}")
  private String redirectUri;

  /**
   * [1] 인가 코드 기반 최초 토큰 발급 및 저장
   */
  @Transactional
  public String storeInitialTokens(User detachedUser, String authorizationCode) {
    User user = getPersistentUser(detachedUser.getId());
    return issueAccessTokenFromAuthorizationCode(user, authorizationCode);
  }

  /**
   * [2] access token 보장: 유효하면 반환, 아니면 재발급
   */
  @Transactional
  public String ensureAccessToken(User detachedUser) {
    User user = getPersistentUser(detachedUser.getId());

    if (isTokenValid(user)) {
      return user.getAccessToken();
    }

    return issueAccessTokenFromAuthorizationCode(user, null);
  }

  /**
   * 영속 상태 User 조회
   */
  private User getPersistentUser(Long userId) {
    return userRepository.findById(userId)
      .orElseThrow(() -> new BaseException(UserErrorCode.USER_NOT_FOUND));
  }

  /**
   * 인가 코드로 토큰 발급 → 저장 후 accessToken 반환
   */
  private String issueAccessTokenFromAuthorizationCode(User user, String code) {
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
      headers.setBasicAuth(clientId, clientSecret);

      String body = "grant_type=authorization_code"
        + "&code=" + code
        + "&redirect_uri=" + redirectUri;

      HttpEntity<String> request = new HttpEntity<>(body, headers);
      ResponseEntity<Map> response = tokenRestTemplate.postForEntity(tokenUri, request, Map.class);

      return updateTokensFromResponse(user, response);
    } catch (RestClientException e) {
      throw new BaseException(HwanbeeErrorCode.TOKEN_REQUEST_FAILED);
    }
  }

  /**
   * 토큰 응답 값을 User 엔티티에 저장
   */
  private String updateTokensFromResponse(User user, ResponseEntity<Map> response) {
    if (!response.getStatusCode().is2xxSuccessful() || !response.hasBody()) {
      throw new BaseException(HwanbeeErrorCode.TOKEN_REQUEST_FAILED);
    }

    Map<String, Object> body = response.getBody();
    String accessToken = (String) body.get("access_token");
    String refreshToken = (String) body.get("refresh_token");
    Integer expiresIn = (Integer) body.get("expires_in");

    if (accessToken == null || refreshToken == null || expiresIn == null) {
      throw new BaseException(HwanbeeErrorCode.TOKEN_REQUEST_FAILED);
    }

    user.updateTokens(accessToken, refreshToken, expiresIn);
    userRepository.save(user); // flush를 유도하여 DB 반영 보장

    return accessToken;
  }

  /**
   * access token 유효성 검사
   */
  private boolean isTokenValid(User user) {
    return user.getAccessToken() != null &&
      user.getExpiresIn() != null;
  }
}
