package com.toninitech.banking.account.application;

import com.toninitech.banking.account.domain.BankAccount;
import com.toninitech.banking.shared.money.Money;
import jakarta.persistence.PessimisticLockException;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

/**
 * The transaction boundary for a money transfer.
 *
 * <p>The domain still owns debit and credit rules. This service owns the
 * all-or-nothing boundary around both changes.</p>
 */
@Service
@AllArgsConstructor
public class TransferApplicationService {

    private final AccountRepository accountRepository;

    @Retryable(retryFor = {PessimisticLockException.class},
    maxAttempts = 4,
    backoff = @Backoff(delay = 2000, multiplier = 2.0, maxDelay = 1000))
    @Transactional
    public void transfer(TransferCommand command){
        Objects.requireNonNull(command, "command is required");

        UUID firstId = command.sourceAccountId().compareTo(command.targetAccountId())
                < 0 ? command.sourceAccountId() : command.targetAccountId();

        UUID secondId = command.sourceAccountId().compareTo(command.targetAccountId())
                < 0 ? command.targetAccountId() : command.sourceAccountId();

        BankAccount firstLock = findRequiredForUpdate(firstId);
        BankAccount secondLock = findRequiredForUpdate(secondId);

        BankAccount source = firstLock.id().equals(command.sourceAccountId()) ? firstLock:secondLock;
        BankAccount target = firstLock.id().equals(command.targetAccountId()) ? firstLock:secondLock;

        source.debit(command.amount());
        accountRepository.save(source);


        target.credit(command.amount());
        accountRepository.save(target);

    }

    private BankAccount findRequired(UUID accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    private BankAccount findRequiredForUpdate(UUID accountId){
        return accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }
}

