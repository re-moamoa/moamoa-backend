package com.hbbhbank.moamoa.wallet.repository;

import com.hbbhbank.moamoa.wallet.domain.Wallet;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.hbbhbank.moamoa.wallet.domain.QCurrency.currency;
import static com.hbbhbank.moamoa.wallet.domain.QWallet.wallet;

@Repository
@RequiredArgsConstructor
public class WalletRepositoryImpl implements WalletRepositoryCustom {

  // JPAQueryFactory: QueryDSL의 핵심 클래스로 JPQL 쿼리를 타입 세이프하게 생성
  private final JPAQueryFactory queryFactory;
  private final EntityManager em;

  /**
   * 사용자 ID와 통화 코드로 특정 지갑을 조회하는 메서드
   * @param userId 사용자 ID
   * @param currencyCode 통화 코드 (예: KRW, USD)
   * @return 조회된 지갑 객체를 Optional로 래핑하여 반환
   */
  @Override
  public Optional<Wallet> findByUserIdAndCurrencyCode(Long userId, String currencyCode) {
    return Optional.ofNullable(
      queryFactory
        .selectFrom(wallet) // wallet 엔티티 전체를 조회 대상으로 설정 (SELECT * FROM wallet)
        // JOIN 절: wallet과 currency를 inner join하고 fetchJoin()으로 즉시 로딩(Eager) 설정
        // fetchJoin()은 연관된 엔티티를 한 번의 쿼리로 함께 가져와 N+1 문제(1번의 쿼리로 N개의 엔티티를 조회한 후, 각 엔티티에 연관된 데이터를 N번 추가 조회) 방지
        .join(wallet.currency, currency).fetchJoin()
        .where(
          wallet.user.id.eq(userId), // 사용자 ID가 일치하는 조건
          wallet.currency.code.eq(currencyCode) // 통화 코드가 일치하는 조건
        )
        .fetchOne() // 결과가 1개 또는 0개. 2개 이상이면 예외
    );
  }

  /**
   * 특정 사용자와 통화 코드 조합의 지갑이 존재하는지 확인하는 메서드
   * @param userId 사용자 ID
   * @param currencyCode 통화 코드
   * @return 존재 여부를 boolean으로 반환
   */
  @Override
  public boolean existsByUserIdAndCurrencyCode(Long userId, String currencyCode) {
    // 성능 최적화를 위해 실제 데이터 대신 상수값 1을 조회
    Integer result = queryFactory
      .selectOne() // SELECT 1과 같은 의미, 실제 컬럼 데이터를 가져오지 않음
      .from(wallet) // FROM 절: wallet 테이블을 대상으로 설정
      .where(
        wallet.user.id.eq(userId),
        wallet.currency.code.eq(currencyCode)
      )
      // fetchFirst(): LIMIT 1과 같은 효과, 첫 번째 결과만 가져옴
      // exists 체크용이므로 모든 데이터를 가져올 필요 없이 하나만 확인하면 충분
      .fetchFirst(); // 성능상 fetchFirst가 더 가볍다

    return result != null; // 결과가 null이 아니면 데이터가 존재
  }

  /**
   * 특정 사용자의 모든 지갑을 조회하는 메서드
   * @param userId 사용자 ID
   * @return 해당 사용자의 모든 지갑 리스트
   */
  @Override
  public List<Wallet> findAllByUser(Long userId) {
    return queryFactory
      .selectFrom(wallet) // wallet 엔티티 전체 조회
      .join(wallet.currency, currency).fetchJoin() // currency 정보도 함께 즉시 로딩하여 지연 로딩으로 인한 추가 쿼리 방지
      .where(wallet.user.id.eq(userId)) // 특정 사용자의 지갑만 필터링
      .fetch(); // fetch(): 여러 개의 결과를 List로 반환
  }

  /**
   * 지갑 번호로 특정 지갑을 조회하는 메서드
   * @param walletNumber 지갑 번호 (고유 식별자)
   * @return 조회된 지갑 객체를 Optional로 래핑하여 반환
   */
  @Override
  public Optional<Wallet> findByWalletNumber(String walletNumber) {
    return Optional.ofNullable(
      queryFactory.selectFrom(wallet)
        .join(wallet.currency, currency).fetchJoin() // 지갑 조회 시 통화 정보도 함께 가져와서 추후 접근 시 추가 쿼리 방지
        .where(wallet.walletNumber.eq(walletNumber)) // 지갑 번호로 필터링
        .fetchOne() // 단일 결과 조회
    );
  }

  /**
   * 지갑 번호로 특정 지갑을 조회하는 메서드 - 락 적용
   * @param walletNumber 지갑 번호 (고유 식별자)
   * @return 조회된 지갑 객체를 Optional로 래핑하여 반환
   */
  @Override
  public Optional<Wallet> findByWalletNumberForUpdate(String walletNumber) {
    // QueryDSL 로 fetchJoin 하면서, PESSIMISTIC_WRITE 락 설정
    Wallet w = queryFactory
      .selectFrom(wallet)
      .join(wallet.currency, currency).fetchJoin()
      .where(wallet.walletNumber.eq(walletNumber))
      .fetchOne();

    if (w != null) {
      // 영속성 컨텍스트에 있는 해당 엔티티에 락 모드 적용
      em.lock(w, LockModeType.PESSIMISTIC_WRITE);
    }
    return Optional.ofNullable(w);
  }

  @Override
  public List<Wallet> findByWalletNumberForUpdateV2(List<String> walletNumbers) {
    List<Wallet> ws = queryFactory
      .selectFrom(wallet)
      .join(wallet.currency, currency).fetchJoin()
      .where(wallet.walletNumber.in(walletNumbers))
      .orderBy(wallet.id.asc())
      .setLockMode(LockModeType.PESSIMISTIC_WRITE)
      .fetch();

    for (Wallet w : ws) {
      em.lock(w, LockModeType.PESSIMISTIC_WRITE);
    }

    return ws;
  }
}
