package com.hbbhbank.moamoa.global.idempotency;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKeyEntity, Long> {

  Optional<IdempotencyKeyEntity> findByIdempotencyKey(String idempotencyKey);

  @Modifying
  @Query("DELETE FROM IdempotencyKeyEntity e WHERE e.createdAt < :threshold")
  int deleteByCreatedAtBefore(@Param("threshold") LocalDateTime threshold);
}
