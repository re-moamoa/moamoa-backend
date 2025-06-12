package com.hbbhbank.moamoa.settlement.dto.response;

import com.hbbhbank.moamoa.user.domain.User;
import com.hbbhbank.moamoa.wallet.dto.response.wallet.SearchWalletResponseDto;

import java.util.List;

public record HostDto(
  Long id,
  String name,
  String email,
  String phoneNumber,
  String profileImage,
  List<SearchWalletResponseDto> wallets
) {
  public static HostDto from(User host) {
    return new HostDto(
      host.getId(),
      host.getName(),
      host.getEmail(),
      host.getPhoneNumber(),
      host.getProfileImage().name(),
      host.getWallets().stream()
        .map(SearchWalletResponseDto::from)
        .toList()
    );
  }
}
