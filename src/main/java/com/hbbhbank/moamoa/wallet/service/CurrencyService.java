package com.hbbhbank.moamoa.wallet.service;

import com.hbbhbank.moamoa.global.exception.BaseException;
import com.hbbhbank.moamoa.wallet.domain.Currency;
import com.hbbhbank.moamoa.wallet.exception.WalletErrorCode;
import com.hbbhbank.moamoa.wallet.repository.CurrencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrencyService {

  private final CurrencyRepository currencyRepository;

  public Currency getByCodeOrThrow(String code) {
    return currencyRepository.findByCode(code)
      .orElseThrow(() -> BaseException.type(WalletErrorCode.CURRENCY_CODE_NOT_FOUND));
  }
}