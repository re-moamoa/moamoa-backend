package com.hbbhbank.moamoa.external.client;

import com.hbbhbank.moamoa.external.dto.request.account.VerificationCodeRequestDto;
import com.hbbhbank.moamoa.external.dto.request.account.VerificationCheckRequestDto;
import com.hbbhbank.moamoa.external.dto.response.account.VerificationCodeResponseDto;
import com.hbbhbank.moamoa.external.dto.response.account.VerificationCheckResponseDto;

public interface HwanbeeAccountClient {

  /**
   * 환비 API에 계좌 인증 코드를 요청합니다. (1원 송금 요청)
   * <p>
   * 이 요청은 사용자가 입력한 외부 은행 계좌 번호에 대해 환비 API 측에서
   * 1회성 입금 인증 코드(보통 소액 입금 또는 숫자 코드)를 발급하게 합니다.
   * 프론트는 이 인증 코드를 사용자에게 입력받아 인증 절차를 마무리합니다.
   * </p>
   *
   * @param dto 인증 코드 요청에 필요한 사용자 ID, 외부 은행 계좌 번호 등의 정보
   * @return 거래 ID, 요청 상태, 1원 송금이 요청되었다는 메세지 (1원 송금은 은행 프론트에서 확인)
   * @throws com.hbbhbank.moamoa.global.exception.BaseException 인증 요청 실패 시 예외 발생
   */
  VerificationCodeResponseDto requestVerificationCode(VerificationCodeRequestDto dto, String accessToken);

  /**
   * 사용자가 입력한 인증 코드의 유효성을 환비 API를 통해 검증합니다.
   * <p>
   * 이 메서드는 사용자가 입력한 코드가 실제 환비 측에서 발급한 코드와 일치하는지 확인하고,
   * 계좌의 인증 여부를 판단합니다. 검증이 성공하면 외부 계좌 ID 등의 정보를 반환합니다.
   * </p>
   *
   * @param dto 인증 검증 요청 객체 (사용자 ID, 외부 계좌 번호, 인증 코드 포함)
   * @return 인증 검증 결과 응답 객체 (성공 여부, 외부 계좌 ID 등)
   * @throws com.hbbhbank.moamoa.global.exception.BaseException 인증 실패 또는 통신 실패 시 예외 발생
   */
  VerificationCheckResponseDto verifyInputCode(VerificationCheckRequestDto dto, String accessToken);

}
