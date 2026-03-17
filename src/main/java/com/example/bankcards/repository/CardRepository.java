package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long>, JpaSpecificationExecutor<Card> {

    @EntityGraph(attributePaths = "user")
    Optional<Card> findWithUserById(Long id);

    @Query("SELECT c FROM Card c WHERE c.user.id = :userId")
    Page<Card> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT c FROM Card c WHERE c.user.id = :userId")
    List<Card> findByUserId(@Param("userId") Long userId);

    @Query("SELECT c FROM Card c WHERE c.cardNumberHash = :hash")
    Optional<Card> findByCardNumberHash(@Param("hash") String hash);

    @Query("SELECT COUNT(c) > 0 FROM Card c WHERE c.user.id = :userId AND c.id = :cardId")
    boolean existsByUserIdAndId(@Param("userId") Long userId, @Param("cardId") Long cardId);

    @Override
    @EntityGraph(attributePaths = "user")
    Page<Card> findAll(Specification<Card> spec, Pageable pageable);
}

