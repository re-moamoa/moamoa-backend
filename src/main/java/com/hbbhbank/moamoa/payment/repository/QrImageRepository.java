package com.hbbhbank.moamoa.payment.repository;

import com.hbbhbank.moamoa.payment.domain.QrImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QrImageRepository extends JpaRepository<QrImage, Long> {
  Optional<QrImage> findByUuid(String uuid);
}
