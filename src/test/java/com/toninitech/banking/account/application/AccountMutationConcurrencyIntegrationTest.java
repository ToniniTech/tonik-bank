package com.toninitech.banking.account.application;

import com.toninitech.banking.account.domain.BankAccount;
import com.toninitech.banking.account.domain.InsufficientFundsException;
import com.toninitech.banking.shared.money.Money;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("postgres")
@Testcontainers(disabledWithoutDocker = true)
class AccountMutationConcurrencyIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private AccountApplicationService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void concurrentDebitsCannotSpendTheSameFundsTwice() throws Exception {
        BankAccount account = savedAccount("100.00");

        List<Boolean> results = runTogether(() -> {
            try {
                accountService.debit(account.id(),
                        new MoneyOperationCommand(new BigDecimal("80.00"), "USD"));
                return true;
            } catch (InsufficientFundsException expected) {
                return false;
            }
        });

        assertEquals(1, results.stream().filter(Boolean::booleanValue).count());
        assertBalance(account.id(), "20.00");
    }

    @Test
    void concurrentCreditsDoNotLoseAnUpdate() throws Exception {
        BankAccount account = savedAccount("0.00");

        runTogether(() -> {
            accountService.credit(account.id(),
                    new MoneyOperationCommand(new BigDecimal("10.00"), "USD"));
            return true;
        });

        assertBalance(account.id(), "20.00");
    }

    private BankAccount savedAccount(String balance) {
        return accountRepository.save(BankAccount.open(
                UUID.randomUUID(), Money.of(balance, "USD")));
    }

    private void assertBalance(UUID accountId, String expected) {
        BankAccount account = accountRepository.findById(accountId).orElseThrow();
        assertEquals(Money.of(expected, "USD"), account.balance());
    }

    private static List<Boolean> runTogether(Callable<Boolean> operation) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        Callable<Boolean> gatedOperation = () -> {
            ready.countDown();
            if (!start.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out waiting for the start signal");
            }
            return operation.call();
        };

        Future<Boolean> first = executor.submit(gatedOperation);
        Future<Boolean> second = executor.submit(gatedOperation);
        try {
            assertTrue(ready.await(10, TimeUnit.SECONDS), "Both workers should be ready");
            start.countDown();
            return List.of(first.get(15, TimeUnit.SECONDS), second.get(15, TimeUnit.SECONDS));
        } finally {
            start.countDown();
            executor.shutdownNow();
        }
    }
}
