package com.hbbhbank.moamoa.user.controller;

import com.hbbhbank.moamoa.global.common.BaseResponse;
import com.hbbhbank.moamoa.global.security.principal.UserPrincipal;
import com.hbbhbank.moamoa.user.domain.ProfileImage;
import com.hbbhbank.moamoa.user.domain.User;
import com.hbbhbank.moamoa.user.dto.request.ChangeNameRequestDto;
import com.hbbhbank.moamoa.user.dto.request.ChangePasswordRequestDto;
import com.hbbhbank.moamoa.user.dto.request.ChangePhoneRequestDto;
import com.hbbhbank.moamoa.user.dto.request.ChangeProfileImageRequestDto;
import com.hbbhbank.moamoa.user.dto.response.HwanbeeTokenResponseDto;
import com.hbbhbank.moamoa.user.dto.response.UserProfileResponseDto;
import com.hbbhbank.moamoa.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

  private final UserService userService;

  @PatchMapping("/password")
  public ResponseEntity<Void> changePassword(@RequestBody @Valid ChangePasswordRequestDto dto) {
    userService.changePassword(dto);
    return ResponseEntity.ok().build();
  }

  @PatchMapping("/phone")
  public ResponseEntity<Void> changePhoneNumber(@RequestBody @Valid ChangePhoneRequestDto dto) {
    userService.changePhoneNumber(dto);
    return ResponseEntity.ok().build();
  }

  @PatchMapping("/name")
  public ResponseEntity<Void> changeUserName(@RequestBody @Valid ChangeNameRequestDto dto) {
    userService.changeUserName(dto);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/profile")
  public ResponseEntity<UserProfileResponseDto> getUserProfile() {
    Long userId = userService.getCurrentUserId();
    UserProfileResponseDto responseDto = userService.getUserProfile(userId);
    return ResponseEntity.ok(responseDto);
  }

  @GetMapping("/name")
  public ResponseEntity<BaseResponse<String>> getUserName() {
    String name = userService.getCurrentUser().getName();
    return ResponseEntity.ok(BaseResponse.success(name));
  }

  @GetMapping("/phone")
  public ResponseEntity<BaseResponse<String>> getUserPhone() {
    String phone = userService.getCurrentUser().getPhoneNumber();
    return ResponseEntity.ok(BaseResponse.success(phone));
  }


  @PatchMapping("/profile-image")
  public ResponseEntity<Void> changeProfileImage(@RequestBody @Valid ChangeProfileImageRequestDto dto) {
    userService.changeProfileImage(dto);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/get-hwanbee-token")
  public ResponseEntity<BaseResponse<HwanbeeTokenResponseDto>> getHwanbeeToken() {
    HwanbeeTokenResponseDto dto = userService.getHwanbeeToken();
    return ResponseEntity.ok(BaseResponse.success(dto));
  }

  /**
   * userId를 통한 특정 사용자 프로필 조회
   * - 정산 그룹 등에서 다른 사용자의 프로필 이미지를 표시할 때 사용
   */
  @GetMapping("/{userId}/profile")
  public ResponseEntity<BaseResponse<UserProfileResponseDto>> getUserProfileById(@PathVariable Long userId) {
    UserProfileResponseDto dto = userService.getUserProfileById(userId);
    return ResponseEntity.ok(BaseResponse.success(dto));
  }
}
