package com.toninitech.banking.account.application;

import com.toninitech.banking.account.domain.BankAccount;
import com.toninitech.banking.shared.money.Money;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TransferLockOrderingTest {

    private static final UUID LOWER_ACCOUNT_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID HIGHER_ACCOUNT_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000002");

    @Test
    void acquiresLocksInAscendingAccountIdOrderRegardlessOfTransferDirection() {
        assertLockOrder(LOWER_ACCOUNT_ID, HIGHER_ACCOUNT_ID);
        assertLockOrder(HIGHER_ACCOUNT_ID, LOWER_ACCOUNT_ID);
    }

    private static void assertLockOrder(UUID sourceId, UUID targetId) {
        AccountRepository repository = mock(AccountRepository.class);
        Map<UUID, BankAccount> accounts = Map.of(
                sourceId, BankAccount.open(sourceId, Money.of("100.00", "USD")),
                targetId, BankAccount.open(targetId, Money.of("100.00", "USD")));
        ArrayList<UUID> acquiredLocks = new ArrayList<>();

        when(repository.findByIdForUpdate(any(UUID.class))).thenAnswer(invocation -> {
            UUID accountId = invocation.getArgument(0);
            acquiredLocks.add(accountId);
            return Optional.of(accounts.get(accountId));
        });

        new TransferApplicationService(repository).transfer(new TransferCommand(
                sourceId, targetId, Money.of("10.00", "USD")));

        assertEquals(
                java.util.List.of(LOWER_ACCOUNT_ID, HIGHER_ACCOUNT_ID),
                acquiredLocks);
    }
}
