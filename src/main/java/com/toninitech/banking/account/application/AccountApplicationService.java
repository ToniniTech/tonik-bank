package com.toninitech.banking.account.application;

import com.toninitech.banking.account.domain.BankAccount;
import com.toninitech.banking.shared.money.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Currency;
import java.util.Objects;
import java.util.UUID;

@Service
public class AccountApplicationService {

    private final AccountRepository accountRepository;

    public AccountApplicationService(AccountRepository accountRepository) {
        this.accountRepository = Objects.requireNonNull(accountRepository);
    }

    public AccountView openAccount(OpenAccountCommand command) {
        Objects.requireNonNull(command, "command is required");
        Money openingBalance = new Money(
                command.openingBalance(),
                Currency.getInstance(command.currencyCode()));
        BankAccount account = BankAccount.open(UUID.randomUUID(), openingBalance);
        return AccountView.from(accountRepository.save(account));
    }

    public AccountView findAccount(UUID accountId) {
        return AccountView.from(findRequired(accountId));
    }

    @Transactional
    public AccountView debit(UUID accountId, MoneyOperationCommand command) {
        BankAccount account = findRequiredForUpdate(accountId);
        account.debit(toMoney(command));
        return AccountView.from(accountRepository.save(account));
    }

    @Transactional
    public AccountView credit(UUID accountId, MoneyOperationCommand command) {
        BankAccount account = findRequiredForUpdate(accountId);
        account.credit(toMoney(command));
        return AccountView.from(accountRepository.save(account));
    }

    private BankAccount findRequired(UUID accountId) {
        Objects.requireNonNull(accountId, "accountId is required");
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    private BankAccount findRequiredForUpdate(UUID accountId) {
        Objects.requireNonNull(accountId, "accountId is required");
        return accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    private static Money toMoney(MoneyOperationCommand command) {
        Objects.requireNonNull(command, "command is required");
        return new Money(command.amount(), Currency.getInstance(command.currencyCode()));
    }
}

