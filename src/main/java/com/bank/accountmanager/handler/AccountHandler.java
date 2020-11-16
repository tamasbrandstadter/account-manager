package com.bank.accountmanager.handler;

import com.bank.accountmanager.handler.request.AccountRequest;
import com.bank.accountmanager.handler.request.OperationRequest;
import com.bank.accountmanager.handler.response.AccountResponse;
import com.bank.accountmanager.repository.AccountRepository;
import com.bank.accountmanager.repository.model.Account;
import com.bank.accountmanager.repository.model.Currency;
import com.bank.accountmanager.service.BalanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component
@Slf4j
@RequiredArgsConstructor
public class AccountHandler {
    private static final String ACCOUNT_ID = "accountId";
    private static final String ACCOUNT_ID_FROM = "accountIdFrom";
    private static final String ACCOUNT_ID_TO = "accountIdTo";

    private final AccountRepository accountRepository;
    private final BalanceService balanceService;

    public Mono<ServerResponse> getAccountBalance(ServerRequest request) {
        return accountRepository.findById(Long.parseLong(request.pathVariable(ACCOUNT_ID)))
                .flatMap(account -> ServerResponse.ok().bodyValue(new AccountResponse(account.getId(), account.getBalance(),
                        account.getCurrency().name(), Instant.now())))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> createAccount(ServerRequest request) {
        return request.bodyToMono(AccountRequest.class)
                .map(this::createAccount)
                .flatMap(accountRepository::save)
                .doOnNext(savedAccount -> log.info("Successfully created account with id {}", savedAccount.getId()))
                .flatMap(createdAccount -> ServerResponse.created(URI.create("/account/%s".formatted(createdAccount.getId())))
                        .contentType(APPLICATION_JSON)
                        .bodyValue(new AccountResponse(createdAccount.getId(), createdAccount.getBalance(),
                                createdAccount.getCurrency().name(), Instant.now())))
                .onErrorResume(this::isCustomerMissing, error -> ServerResponse.notFound().build());
    }

    @Transactional
    public Mono<ServerResponse> deposit(ServerRequest request) {
        var accountId = Long.parseLong(request.pathVariable(ACCOUNT_ID));

        return accountRepository.findById(accountId)
                .flatMap(account -> request.bodyToMono(OperationRequest.class)
                        .map(OperationRequest::amount)
                        .flatMap(amount -> balanceService.addAmountToBalance(account, amount)))
                .flatMap(accountRepository::save)
                .doOnNext(account -> log.info("Successfully deposited amount for account {}, balance {}",
                        account.getId(), account.getBalance()))
                .flatMap(updatedAccount -> ServerResponse.ok().contentType(APPLICATION_JSON)
                        .bodyValue(new AccountResponse(updatedAccount.getId(), updatedAccount.getBalance(),
                                updatedAccount.getCurrency().name(), Instant.now())))
                .onErrorResume(error -> ServerResponse.badRequest().bodyValue(error.getMessage()))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    @Transactional
    public Mono<ServerResponse> withdraw(ServerRequest request) {
        var accountId = request.pathVariable(ACCOUNT_ID);

        return accountRepository.findById(Long.parseLong(accountId))
                .flatMap(account -> request.bodyToMono(OperationRequest.class)
                        .map(OperationRequest::amount)
                        .flatMap(amount -> balanceService.subtractAmountFromBalance(account, amount)))
                .flatMap(accountRepository::save)
                .doOnNext(account -> log.info("Successfully withdrew amount from account {}, balance {}",
                        account.getId(), account.getBalance()))
                .flatMap(updatedAccount -> ServerResponse.ok().contentType(APPLICATION_JSON)
                        .bodyValue(new AccountResponse(updatedAccount.getId(), updatedAccount.getBalance(),
                                updatedAccount.getCurrency().name(), Instant.now())))
                .onErrorResume(error -> ServerResponse.badRequest().contentType(APPLICATION_JSON).bodyValue(error.getMessage()))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    @Transactional
    public Mono<ServerResponse> transfer(ServerRequest request) {
        var idFrom = Long.parseLong(request.pathVariable(ACCOUNT_ID_FROM));
        var idTo = Long.parseLong(request.pathVariable(ACCOUNT_ID_TO));

        // note: currency conversion logic should be added, but it is out of task scope

        return accountRepository.findById(idFrom)
                .zipWith(accountRepository.findById(idTo))
                .flatMap(accounts -> request.bodyToMono(OperationRequest.class)
                        .map(OperationRequest::amount)
                        .flatMap(amount -> balanceService.modifyBalances(accounts, amount)))
                .map(accounts -> accountRepository.saveAll(Set.of(accounts.getT1(), accounts.getT2()))
                        .doOnNext(account -> log.info("Successfully updated account {} with transfer, balance {}", account.getId(), account.getBalance())))
                .flatMap(updatedAccounts -> ServerResponse.ok().contentType(APPLICATION_JSON).body(updatedAccounts, Account.class))
                .onErrorResume(error -> ServerResponse.badRequest().contentType(APPLICATION_JSON).bodyValue(error.getMessage()))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    private Account createAccount(AccountRequest request) {
        var account = new Account(Currency.valueOf(request.currency()), request.customerId(), Instant.now());
        Optional.ofNullable(request.initialDeposit()).ifPresent(account::setBalance);
        return account;
    }

    private boolean isCustomerMissing(Throwable t) {
        if (t instanceof DataIntegrityViolationException ex) {
            log.error("Could not find customer id in customers table, message {}", ex.getMessage());
            return true;
        }
        return false;
    }

}
