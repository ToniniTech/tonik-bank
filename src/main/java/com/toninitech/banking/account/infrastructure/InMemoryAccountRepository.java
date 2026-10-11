package com.toninitech.banking.account.infrastructure;

import com.toninitech.banking.account.application.AccountRepository;
import com.toninitech.banking.account.domain.BankAccount;
import com.toninitech.banking.account.infrastructure.persistence.AccountJpaEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;
import org.springframework.context.annotation.Profile;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
@Profile("in-memory")
public class InMemoryAccountRepository implements AccountRepository {

    private final ConcurrentMap<UUID, BankAccount> accounts = new ConcurrentHashMap<>();

    @Override
    public BankAccount save(BankAccount account) {
        Objects.requireNonNull(account, "account is required");
        accounts.put(account.id(), account);
        return account;
    }

    @Override
    public Optional<BankAccount> findById(UUID accountId) {
        Objects.requireNonNull(accountId, "accountId is required");
        return Optional.ofNullable(accounts.get(accountId));
    }

    @Override
    public Optional<BankAccount> findByIdForUpdate(UUID accountId) {
        Objects.requireNonNull(accountId, "accountId is required");
        return Optional.ofNullable(accounts.get(accountId));
    }
}
