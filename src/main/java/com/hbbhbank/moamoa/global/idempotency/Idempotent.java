package com.hbbhbank.moamoa.global.idempotency;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 멱등성 보장이 필요한 API 메서드에 선언한다.
 *
 * 이 애노테이션이 붙은 메서드는 {@link IdempotencyAspect}에 의해
 * 요청 헤더의 {@code Idempotency-Key}를 기준으로 중복 실행을 차단한다.
 *
 * <p>적용 대상: 금액 변동이 수반되는 POST 요청 (송금, 결제, 충전 등)</p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {
}
