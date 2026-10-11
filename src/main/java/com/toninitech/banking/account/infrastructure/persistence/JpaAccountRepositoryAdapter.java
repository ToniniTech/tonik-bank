package com.toninitech.banking.account.infrastructure.persistence;

import com.toninitech.banking.account.application.AccountRepository;
import com.toninitech.banking.account.domain.BankAccount;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Repository
@Profile("!in-memory")
public class JpaAccountRepositoryAdapter implements AccountRepository {

    private final SpringDataAccountJpaRepository repository;

    public JpaAccountRepositoryAdapter(SpringDataAccountJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public BankAccount save(BankAccount account) {
        Objects.requireNonNull(account, "account is required");
        AccountJpaEntity entity = repository.findById(account.id())
                .map(existing -> update(existing, account))
                .orElseGet(() -> AccountJpaEntity.fromDomain(account));
        return repository.save(entity).toDomain();
    }

    @Override
    public Optional<BankAccount> findById(UUID accountId) {
        Objects.requireNonNull(accountId, "accountId is required");
        return repository.findById(accountId).map(AccountJpaEntity::toDomain);
    }

    @Override
    public Optional<BankAccount> findByIdForUpdate(UUID accountId) {
        return repository.findByIdForUpdate(accountId)
                .map(AccountJpaEntity::toDomain);
    }

    private static AccountJpaEntity update(AccountJpaEntity entity, BankAccount account) {
        entity.updateFrom(account);
        return entity;
    }
}
