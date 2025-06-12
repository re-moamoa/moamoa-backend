package com.hbbhbank.moamoa.transfer.service;

import com.hbbhbank.moamoa.transfer.dto.request.PointTransferRequestDto;
import com.hbbhbank.moamoa.transfer.dto.response.PointTransferResponseDto;
import com.hbbhbank.moamoa.wallet.domain.Wallet;

import java.util.List;

public interface PointTransferService {

  PointTransferResponseDto transferByUserV1(PointTransferRequestDto dto);

  PointTransferResponseDto transferByUserV2(PointTransferRequestDto dto);

  PointTransferResponseDto transferByUserV3(PointTransferRequestDto dto);
}
