package com.toninitech.banking.account.application;

import com.toninitech.banking.account.domain.BankAccount;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository {

    BankAccount save(BankAccount account);

    Optional<BankAccount> findById(UUID accountId);

    Optional<BankAccount> findByIdForUpdate(UUID accountId);


}

