package com.bank.accountmanager.service;

import com.bank.accountmanager.handler.exception.InvalidAmountException;
import com.bank.accountmanager.repository.model.Account;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.math.BigDecimal;
import java.time.Instant;

import static com.bank.accountmanager.repository.model.Currency.EUR;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BalanceServiceTest {

    private final BalanceService balanceService = new BalanceService();

    @Test
    public void shouldAddAmountToBalance() {
        var account = new Account(EUR, 123L, Instant.now());

        var ten = BigDecimal.TEN;

        Mono<Account> accountMono = balanceService.addAmountToBalance(account, ten);

        accountMono.subscribe(acc -> assertEquals(10, acc.getBalance().intValue()));
    }

    @Test
    public void shouldReturnErrorWhenAmountIsInvalid() {
        var account = new Account(EUR, 123L, Instant.now());

        var zero = BigDecimal.ZERO;

        Mono<Account> accountMono = balanceService.addAmountToBalance(account, zero);

        StepVerifier.create(accountMono)
                .expectErrorMatches(t -> t instanceof InvalidAmountException
                        && t.getMessage().equals("Deposit amount must be greater than 0"))
                .verify();
    }

    @Test
    public void shouldSubtractAmountFromBalance() {
        var account = new Account(EUR, 123L, Instant.now());
        account.setBalance(BigDecimal.valueOf(21));

        var ten = BigDecimal.TEN;

        Mono<Account> accountMono = balanceService.subtractAmountFromBalance(account, ten);

        accountMono.subscribe(acc -> assertEquals(11, acc.getBalance().intValue()));
    }

    @Test
    public void shouldReturnErrorWhenAmountIsInvalidForSubtract() {
        var account = new Account(EUR, 123L, Instant.now());
        account.setBalance(BigDecimal.TEN);

        var eleven = BigDecimal.valueOf(11);

        Mono<Account> accountMono = balanceService.subtractAmountFromBalance(account, eleven);

        StepVerifier.create(accountMono)
                .expectErrorMatches(t -> t instanceof InvalidAmountException
                        && t.getMessage().equals("Insufficient funds, cannot withdraw 11, balance 10"))
                .verify();

        Mono<Account> otherAccountMono = balanceService.subtractAmountFromBalance(account, BigDecimal.ZERO);

        StepVerifier.create(otherAccountMono)
                .expectErrorMatches(t -> t instanceof InvalidAmountException
                        && t.getMessage().equals("Withdraw amount must be greater than 0"))
                .verify();
    }

    @Test
    public void shouldModifyBalances() {
        var account = new Account(EUR, 1L, Instant.now());
        account.setBalance(BigDecimal.TEN);

        var account2 = new Account(EUR, 2L, Instant.now());

        BigDecimal five = BigDecimal.valueOf(4);

        Tuple2<Account, Account> accountTuple = Tuples.of(account, account2);

        Mono<Tuple2<Account, Account>> tupleMono = balanceService.modifyBalances(accountTuple, five);

        tupleMono.subscribe(accounts -> {
            assertEquals(6, accounts.getT1().getBalance().intValue());
            assertEquals(4, accounts.getT2().getBalance().intValue());
        });
    }

    @Test
    public void shouldReturnErrorWhenTransferAmountIsZeroOrNegative() {
        var account = new Account(EUR, 1L, Instant.now());
        account.setBalance(BigDecimal.TEN);

        var account2 = new Account(EUR, 2L, Instant.now());

        BigDecimal zero = BigDecimal.ZERO;

        Tuple2<Account, Account> accountTuple = Tuples.of(account, account2);

        Mono<Tuple2<Account, Account>> tupleMono = balanceService.modifyBalances(accountTuple, zero);

        StepVerifier.create(tupleMono)
                .expectErrorMatches(t -> t instanceof InvalidAmountException
                        && t.getMessage().equals("Transfer amount must be greater than 0"))
                .verify();

        BigDecimal minusOne = BigDecimal.valueOf(-1);

        Tuple2<Account, Account> otherAccountTuple = Tuples.of(account, account2);

        Mono<Tuple2<Account, Account>> otherTupleMono = balanceService.modifyBalances(otherAccountTuple, minusOne);

        StepVerifier.create(otherTupleMono)
                .expectErrorMatches(t -> t instanceof InvalidAmountException
                        && t.getMessage().equals("Transfer amount must be greater than 0"))
                .verify();
    }

    @Test
    public void shouldReturnErrorWhenInsufficientFunds() {
        var account = new Account(EUR, 1L, Instant.now());
        account.setBalance(BigDecimal.TEN);

        var account2 = new Account(EUR, 2L, Instant.now());

        BigDecimal zero = BigDecimal.valueOf(15);

        Tuple2<Account, Account> accountTuple = Tuples.of(account, account2);

        Mono<Tuple2<Account, Account>> tupleMono = balanceService.modifyBalances(accountTuple, zero);

        StepVerifier.create(tupleMono)
                .expectErrorMatches(t -> t instanceof InvalidAmountException
                        && t.getMessage().equals("Insufficient funds, cannot transfer 15, balance 10"))
                .verify();
    }

}
