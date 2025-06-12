package com.hbbhbank.moamoa.external.auth;

import com.hbbhbank.moamoa.external.dto.request.OAuth2TokenRequestDto;
import com.hbbhbank.moamoa.global.common.BaseResponse;
import com.hbbhbank.moamoa.global.exception.BaseException;
import com.hbbhbank.moamoa.user.domain.User;
import com.hbbhbank.moamoa.user.exception.UserErrorCode;
import com.hbbhbank.moamoa.user.repository.UserRepository;
import com.hbbhbank.moamoa.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/oauth2")
public class OAuth2CallbackController {

  private final UserRepository userRepository;
  private final OAuth2TokenService oAuth2TokenService;
  private final UserService userService;

  /**
   * 프론트에서 전달받은 인가 코드로 AccessToken 발급 요청
   */
  @PostMapping("/token")
  public ResponseEntity<BaseResponse<String>> requestToken(
    @RequestBody @Valid OAuth2TokenRequestDto req
  ) {
    Long userId = userService.getCurrentUserId();
    User user = userRepository.findById(userId)
      .orElseThrow(() -> new BaseException(UserErrorCode.USER_NOT_FOUND));

    // 인가 코드로 액세스 토큰 발급 및 저장
    String accessToken = oAuth2TokenService.storeInitialTokens(user, req.code());
    return ResponseEntity.ok(BaseResponse.success(accessToken));
  }

  /**
   * 사용자 환비 인증 여부 조회 (access token이 있고 유효한지)
   */
  @GetMapping("/status")
  public ResponseEntity<BaseResponse<Boolean>> checkHwanbeeAuthenticated() {
    Long userId = userService.getCurrentUserId();
    User user = userRepository.findById(userId)
      .orElseThrow(() -> new BaseException(UserErrorCode.USER_NOT_FOUND));

    boolean authenticated = user.getAccessToken() != null &&
      user.getExpiresIn() != null;

    return ResponseEntity.ok(BaseResponse.success(authenticated));
  }
}
