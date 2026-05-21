package com.hbbhbank.moamoa.global.idempotency;

import com.hbbhbank.moamoa.global.exception.BaseException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

/**
 * {@link Idempotent} 애노테이션이 선언된 컨트롤러 메서드를 가로채어
 * 멱등성 키 기반 중복 요청 차단 로직을 수행하는 AOP Aspect.
 *
 * <h3>동작 방식</h3>
 * <ol>
 *   <li>HTTP 요청 헤더에서 {@code Idempotency-Key} 값을 추출한다.</li>
 *   <li>DB에 해당 키가 이미 존재하면, 저장된 응답을 그대로 반환한다 (중복 실행 차단).</li>
 *   <li>키가 없으면 실제 로직({@code proceed()})을 실행하고, 성공 응답을 키와 함께 DB에 저장한다.</li>
 *   <li>동시 요청으로 Unique 제약조건 위반이 발생하면, 먼저 완료된 요청의 응답을 반환한다.</li>
 * </ol>
 *
 * <h3>트랜잭션 설계 — FacadeService 패턴</h3>
 * <p>
 * DB 조작은 {@link IdempotencyKeyService}에 위임한다.
 * 이 Aspect는 컨트롤러 레벨에서 동작하여 비즈니스 트랜잭션(@Transactional) 바깥에 위치하므로,
 * 각 DB 호출이 독립적인 짧은 트랜잭션으로 실행된다.
 * </p>
 * <p>
 * 이를 통해 두 가지 문제를 방지한다:
 * <ul>
 *   <li><b>REQUIRES_NEW 데드락</b>: 중첩 트랜잭션이 별도 커넥션을 점유하여
 *       커넥션 풀 고갈 시 발생하는 데드락을 원천 차단한다.</li>
 *   <li><b>Self-invocation</b>: 같은 클래스 내부 호출은 Spring 프록시를 우회하여
 *       @Transactional이 동작하지 않는 문제를 서비스 분리로 해결한다.</li>
 * </ul>
 * </p>
 *
 * @see <a href="https://xxeol.tistory.com/55">[Spring] REQUIRES_NEW와 데드락 위험성</a>
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class IdempotencyAspect {

  private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

  private final IdempotencyKeyService idempotencyKeyService;

  @Around("@annotation(com.hbbhbank.moamoa.global.idempotency.Idempotent)")
  public Object checkIdempotency(ProceedingJoinPoint joinPoint) throws Throwable {
    String key = extractIdempotencyKey();

    // 1. 기존 키 조회 — 이미 처리된 요청이면 저장된 응답 반환
    //    독립 트랜잭션(readOnly)으로 실행되어 비즈니스 트랜잭션과 무관
    Optional<IdempotencyKeyEntity> existing = idempotencyKeyService.findByKey(key);
    if (existing.isPresent()) {
      log.info("멱등성 키 중복 감지, 캐싱된 응답 반환: key={}", key);
      return idempotencyKeyService.restoreResponse(existing.get());
    }

    // 2. 실제 비즈니스 로직 실행
    //    컨트롤러 → 서비스(@Transactional) → 커밋 후 반환
    Object result = joinPoint.proceed();

    // 3. 성공 응답 저장
    //    비즈니스 트랜잭션이 이미 커밋된 후이므로, 별도의 짧은 트랜잭션으로 저장
    //    → REQUIRES_NEW 불필요, 커넥션 1개로 순차 실행
    try {
      idempotencyKeyService.save(key, result);
    } catch (DataIntegrityViolationException e) {
      // 동시 요청으로 Unique 제약조건 위반 → 먼저 완료된 요청의 응답 반환
      log.info("멱등성 키 동시 삽입 감지, 기존 응답 반환: key={}", key);
      return idempotencyKeyService.findByKey(key)
          .<Object>map(idempotencyKeyService::restoreResponse)
          .orElse(result);
    }

    return result;
  }

  private String extractIdempotencyKey() {
    ServletRequestAttributes attrs =
        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    if (attrs == null) {
      throw new BaseException(IdempotencyErrorCode.MISSING_IDEMPOTENCY_KEY);
    }

    HttpServletRequest request = attrs.getRequest();
    String key = request.getHeader(IDEMPOTENCY_KEY_HEADER);
    if (key == null || key.isBlank()) {
      throw new BaseException(IdempotencyErrorCode.MISSING_IDEMPOTENCY_KEY);
    }
    return key.trim();
  }
}
