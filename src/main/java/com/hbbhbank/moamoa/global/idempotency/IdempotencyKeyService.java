package com.hbbhbank.moamoa.global.idempotency;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 멱등성 키의 조회·저장을 담당하는 서비스.
 *
 * <h3>왜 Aspect와 분리했는가? (FacadeService 패턴)</h3>
 * <p>
 * Spring AOP는 프록시 기반으로 동작하므로, 같은 클래스 내부에서
 * {@code this.saveIdempotencyKey()}를 호출하면 프록시를 우회하여
 * {@code @Transactional}이 적용되지 않는다 (self-invocation 문제).
 * </p>
 * <p>
 * 또한 {@code REQUIRES_NEW}를 사용하면 새 트랜잭션이 별도의 DB 커넥션을 점유하는데,
 * 외부 트랜잭션이 커넥션을 잡고 있는 상태에서 커넥션 풀이 고갈되면
 * 내부 트랜잭션이 커넥션을 얻지 못해 데드락이 발생할 수 있다.
 * </p>
 * <p>
 * 이를 해결하기 위해 트랜잭션 경계를 별도 서비스로 분리하였다.
 * {@link IdempotencyAspect}는 컨트롤러 레벨에서 동작하므로
 * 서비스의 비즈니스 트랜잭션 바깥에서 호출된다.
 * 따라서 각 메서드가 독립적인 짧은 트랜잭션으로 실행되며,
 * 중첩 트랜잭션이나 추가 커넥션 점유 없이 안전하게 동작한다.
 * </p>
 *
 * @see <a href="https://xxeol.tistory.com/55">[Spring] REQUIRES_NEW와 데드락 위험성</a>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyKeyService {

  private final IdempotencyKeyRepository idempotencyKeyRepository;
  private final ObjectMapper objectMapper;

  @Transactional(readOnly = true)
  public Optional<IdempotencyKeyEntity> findByKey(String key) {
    return idempotencyKeyRepository.findByIdempotencyKey(key);
  }

  @Transactional
  public void save(String key, Object result) {
    int httpStatus = HttpStatus.OK.value();
    String body;

    try {
      if (result instanceof ResponseEntity<?> responseEntity) {
        httpStatus = responseEntity.getStatusCode().value();
        body = objectMapper.writeValueAsString(responseEntity.getBody());
      } else {
        body = objectMapper.writeValueAsString(result);
      }
    } catch (Exception e) {
      log.warn("멱등성 키 응답 직렬화 실패 (비즈니스 로직에는 영향 없음): key={}", key, e);
      return;
    }

    IdempotencyKeyEntity entity = IdempotencyKeyEntity.builder()
        .idempotencyKey(key)
        .httpStatus(httpStatus)
        .responseBody(body)
        .build();

    idempotencyKeyRepository.save(entity);
    log.debug("멱등성 키 저장 완료: key={}", key);
  }

  public ResponseEntity<Object> restoreResponse(IdempotencyKeyEntity entity) {
    try {
      Object body = objectMapper.readValue(entity.getResponseBody(), Object.class);
      return ResponseEntity
          .status(entity.getHttpStatus())
          .body(body);
    } catch (Exception e) {
      log.warn("멱등성 키 응답 역직렬화 실패: key={}", entity.getIdempotencyKey(), e);
      throw new RuntimeException("캐싱된 응답 복원 실패", e);
    }
  }
}
