package com.hbbhbank.moamoa.wallet.dto.response.wallet;

public record CreateWalletResponseDto(
  String hwanbeeAccountNumber
) {
  public static CreateWalletResponseDto from(String hwanbeeAccountNumber) {
    return new CreateWalletResponseDto(hwanbeeAccountNumber);
  }
}
