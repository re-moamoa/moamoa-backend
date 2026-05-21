package com.hbbhbank.moamoa.global.idempotency;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 멱등성 키를 저장하는 엔티티.
 *
 * 클라이언트가 전달한 Idempotency-Key와 해당 요청의 응답을 함께 저장하여,
 * 동일 키로 재요청 시 원래 응답을 그대로 반환한다.
 *
 * <p>Unique 제약조건으로 동시 요청(race condition) 시 DB 레벨에서 중복 삽입을 차단한다.</p>
 */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "idempotency_keys",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_idempotency_key",
        columnNames = "idempotency_key"
    )
)
public class IdempotencyKeyEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "idempotency_key", nullable = false, length = 64)
  private String idempotencyKey;

  @Column(name = "http_status", nullable = false)
  private int httpStatus;

  @Lob
  @Column(name = "response_body", nullable = false, columnDefinition = "TEXT")
  private String responseBody;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  public void prePersist() {
    this.createdAt = LocalDateTime.now();
  }

  @Builder
  public IdempotencyKeyEntity(String idempotencyKey, int httpStatus, String responseBody) {
    this.idempotencyKey = idempotencyKey;
    this.httpStatus = httpStatus;
    this.responseBody = responseBody;
  }
}
