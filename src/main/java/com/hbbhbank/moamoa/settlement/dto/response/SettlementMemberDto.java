package com.hbbhbank.moamoa.settlement.dto.response;

import com.hbbhbank.moamoa.settlement.domain.SettlementMember;
import com.hbbhbank.moamoa.wallet.dto.response.wallet.SearchWalletResponseDto;

import java.util.List;

public record SettlementMemberDto(
  Long userId,
  String name,
  String profileImage,
  List<SearchWalletResponseDto> wallets,
  boolean hasTransferred
) {
  public static SettlementMemberDto from(SettlementMember member) {
    return new SettlementMemberDto(
      member.getUser().getId(),
      member.getUser().getName(),
      member.getUser().getProfileImage().name(),
      member.getUser().getWallets().stream()
        .map(SearchWalletResponseDto::from)
        .toList(),
      member.isHasTransferred()
    );
  }
}
