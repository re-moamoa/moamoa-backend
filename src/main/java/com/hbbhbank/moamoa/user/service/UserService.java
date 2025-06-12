package com.hbbhbank.moamoa.user.service;

import com.hbbhbank.moamoa.global.exception.BaseException;
import com.hbbhbank.moamoa.global.security.util.SecurityUtil;
import com.hbbhbank.moamoa.user.domain.ProfileImage;
import com.hbbhbank.moamoa.user.domain.User;
import com.hbbhbank.moamoa.user.dto.request.ChangeNameRequestDto;
import com.hbbhbank.moamoa.user.dto.request.ChangePasswordRequestDto;
import com.hbbhbank.moamoa.user.dto.request.ChangePhoneRequestDto;
import com.hbbhbank.moamoa.user.dto.request.ChangeProfileImageRequestDto;
import com.hbbhbank.moamoa.user.dto.response.HwanbeeTokenResponseDto;
import com.hbbhbank.moamoa.user.dto.response.UserProfileResponseDto;
import com.hbbhbank.moamoa.user.exception.UserErrorCode;
import com.hbbhbank.moamoa.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  /**
   * 현재 로그인된 사용자의 비밀번호 변경
   */
  @Transactional
  public void changePassword(ChangePasswordRequestDto dto) {
    User user = getCurrentUser();
    user.validatePassword(dto.oldPassword(), passwordEncoder);
    user.changePassword(passwordEncoder.encode(dto.newPassword()));
  }

  /**
   * 현재 로그인된 사용자의 전화번호 변경
   */
  @Transactional
  public void changePhoneNumber(ChangePhoneRequestDto dto) {
    String newPhoneNumber = dto.phoneNumber();

    // 전화번호 중복 확인
    if (userRepository.existsByPhoneNumber(newPhoneNumber)) {
      throw BaseException.type(UserErrorCode.INVALID_PHONE); // 이미 사용 중인 번호
    }

    User user = getCurrentUser();
    user.changePhoneNumber(newPhoneNumber);
  }


  /**
   * 현재 로그인된 사용자의 이름 변경
   */
  @Transactional
  public void changeUserName(ChangeNameRequestDto dto) {
    User user = getCurrentUser();
    user.changeUserName(dto.name());
  }

  /**
   * 현재 로그인된 사용자의 프로필 이미지 변경
   */
  @Transactional
  public void changeProfileImage(ChangeProfileImageRequestDto dto) {
    User user = getCurrentUser();
    ProfileImage newImage = ProfileImage.from(dto.profileImage());
    user.updateProfileImage(newImage);
  }

  /**
   * 현재 로그인된 사용자 ID 조회
   */
  public Long getCurrentUserId() {
    return SecurityUtil.getCurrentUserId();
  }

  /**
   * 현재 로그인된 사용자 조회
   */
  public User getCurrentUser() {
    return getByIdOrThrow(getCurrentUserId());
  }

  /**
   * 사용자 ID로 사용자 조회 (예외 포함)
   */
  public User getByIdOrThrow(Long userId) {
    return userRepository.findById(userId)
      .orElseThrow(() -> BaseException.type(UserErrorCode.USER_NOT_FOUND));
  }

  /**
   * 사용자 프로필 정보 조회
   */
  public UserProfileResponseDto getUserProfile(Long userId) {
    User user = getByIdOrThrow(userId);
    String profileImageValue = null;

    if (user.getProfileImage() != null) {
      profileImageValue = ProfileImage.fromKey(user.getProfileImage().name()).getValue(); // IMAGE1 → img_1
    }

    return new UserProfileResponseDto(user.getName(), profileImageValue);
  }

  public HwanbeeTokenResponseDto getHwanbeeToken() {
    User user = getByIdOrThrow(getCurrentUserId());

    String accessToken = user.getAccessToken();
    String refreshToken = user.getRefreshToken();
    Integer expiresIn = user.getExpiresIn();

    if (accessToken == null || refreshToken == null || expiresIn == null) {
      throw BaseException.type(UserErrorCode.HWANBEE_TOKEN_NOT_FOUND);
    }

    return new HwanbeeTokenResponseDto(accessToken, refreshToken, expiresIn);
  }

  public UserProfileResponseDto getUserProfileById(Long userId) {
    User user = userRepository.findById(userId)
      .orElseThrow(() -> new BaseException(UserErrorCode.USER_NOT_FOUND));

    String profileImageValue = null;

    if (user.getProfileImage() != null) {
      profileImageValue = ProfileImage.fromKey(user.getProfileImage().name()).getValue(); // IMAGE1 → img_1
    }

    return UserProfileResponseDto.from(user.getName(), profileImageValue);
  }
}
