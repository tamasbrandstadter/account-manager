package com.bank.accountmanager.service;

import com.bank.accountmanager.handler.exception.InvalidAmountException;
import com.bank.accountmanager.repository.model.Account;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.math.BigDecimal;

@Service
public class BalanceService {

    public Mono<Account> addAmountToBalance(Account account, BigDecimal amount) {
        if (isLessOrEqualToZero(amount)) {
            return Mono.error(new InvalidAmountException("Deposit amount must be greater than 0"));
        }
        account.setBalance(account.getBalance().add(amount));
        return Mono.just(account);
    }

    public Mono<Account> subtractAmountFromBalance(Account account, BigDecimal amount) {
        if (isLessOrEqualToZero(amount)) {
            return Mono.error(new InvalidAmountException("Withdraw amount must be greater than 0"));
        }
        if (isInsufficientBalance(account.getBalance(), amount)) {
            return Mono.error(new InvalidAmountException("Insufficient funds, cannot withdraw %s, balance %s"
                    .formatted(amount, account.getBalance())));
        }
        account.setBalance(account.getBalance().subtract(amount));
        return Mono.just(account);
    }

    public Mono<Tuple2<Account, Account>> modifyBalances(Tuple2<Account, Account> accounts, BigDecimal amount) {
        if (isLessOrEqualToZero(amount)) {
            return Mono.error(new InvalidAmountException("Transfer amount must be greater than 0"));
        }

        Account from = accounts.getT1();
        BigDecimal balanceFrom = from.getBalance();
        if (isInsufficientBalance(balanceFrom, amount)) {
            return Mono.error(new InvalidAmountException("Insufficient funds, cannot transfer %s, balance %s"
                    .formatted(amount, balanceFrom)));
        }
        from.setBalance(balanceFrom.subtract(amount));

        Account to = accounts.getT2();
        to.setBalance(to.getBalance().add(amount));
        return Mono.just(accounts);
    }

    private boolean isLessOrEqualToZero(BigDecimal amount) {
        return amount.signum() <= 0;
    }

    private boolean isInsufficientBalance(BigDecimal balance, BigDecimal amount) {
        return balance.compareTo(amount) < 0;
    }

}
