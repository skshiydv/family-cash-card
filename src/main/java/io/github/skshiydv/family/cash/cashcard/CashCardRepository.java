package io.github.skshiydv.family.cash.cashcard;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface CashCardRepository extends CrudRepository<CashCard, Long>, PagingAndSortingRepository<CashCard, Long> {
    CashCard findByIdAndOwner(Long id, String owner);

    Page<CashCard> findByOwner(String owner, Pageable pageable);

    boolean existsByIdAndOwner(long id, String owner);
}
