package com.toninitech.banking.account.api;

import com.toninitech.banking.account.application.TransferCommand;
import com.toninitech.banking.shared.money.Money;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

public record TransferRequest(
        @NotNull UUID sourceAccountId,
        @NotNull UUID targetAccountId,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal amount,
        @NotBlank @Pattern(regexp = "[A-Z]{3}") String currency) {

    public TransferCommand toCommand() {
        return new TransferCommand(
                sourceAccountId,
                targetAccountId,
                new Money(amount, Currency.getInstance(currency)));
    }
}
