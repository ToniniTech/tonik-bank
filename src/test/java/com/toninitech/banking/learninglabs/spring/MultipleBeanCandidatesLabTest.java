package com.toninitech.banking.learninglabs.spring;

import com.toninitech.banking.account.application.AccountApplicationService;
import com.toninitech.banking.account.application.AccountRepository;
import com.toninitech.banking.account.domain.BankAccount;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MultipleBeanCandidatesLabTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(AmbiguousConfiguration.class);

    @Test
    void contextFailsWhenTwoBeansMatchTheSameDependency() {
        contextRunner.run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure())
                    .hasMessageContaining("AccountRepository")
                    .hasMessageContaining("repositoryOne")
                    .hasMessageContaining("repositoryTwo");
        });
    }

    @Configuration(proxyBeanMethods = false)
    static class AmbiguousConfiguration {

        @Bean
        AccountRepository repositoryOne() {
            return new EmptyAccountRepository();
        }

        @Bean
        AccountRepository repositoryTwo() {
            return new EmptyAccountRepository();
        }

        @Bean
        AccountApplicationService accountApplicationService(AccountRepository repository) {
            return new AccountApplicationService(repository);
        }
    }

    private static final class EmptyAccountRepository implements AccountRepository {

        @Override
        public BankAccount save(BankAccount account) {
            return account;
        }

        @Override
        public Optional<BankAccount> findById(UUID accountId) {
            return Optional.empty();
        }

        @Override
        public Optional<BankAccount> findByIdForUpdate(UUID accountId) {
            return Optional.empty();
        }
    }
}

