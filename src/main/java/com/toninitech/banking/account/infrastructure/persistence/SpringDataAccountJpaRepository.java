package com.toninitech.banking.account.infrastructure.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

interface SpringDataAccountJpaRepository extends JpaRepository<AccountJpaEntity, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    select a
    from AccountJpaEntity a
    where a.id = :id
""")
    Optional<AccountJpaEntity> findByIdForUpdate(@Param("id") UUID id);
}
