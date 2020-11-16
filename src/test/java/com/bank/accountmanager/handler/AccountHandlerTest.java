package com.bank.accountmanager.handler;

import com.bank.accountmanager.handler.request.AccountRequest;
import com.bank.accountmanager.handler.request.OperationRequest;
import com.bank.accountmanager.repository.AccountRepository;
import com.bank.accountmanager.repository.model.Account;
import com.bank.accountmanager.service.BalanceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.math.BigDecimal;
import java.time.Instant;

import static com.bank.accountmanager.repository.model.Currency.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AccountHandlerTest {

    @Mock(answer = RETURNS_DEEP_STUBS)
    private AccountRepository accountRepository;

    @Mock
    private BalanceService balanceService;

    @Captor
    private ArgumentCaptor<Account> accountCaptor;

    @InjectMocks
    private AccountHandler accountHandler;

    @Test
    public void shouldGetAccountBalance() {
        var usdAccount = new Account(USD, 12L, Instant.now());

        when(accountRepository.findById(12L)).thenReturn(Mono.just(usdAccount));

        var request = MockServerRequest.builder()
                .pathVariable("accountId", "12")
                .build();

        accountHandler.getAccountBalance(request).subscribe(response -> assertTrue(response.statusCode().is2xxSuccessful()));
    }

    @Test
    public void shouldCreateAccount() {
        var accountRequest = new AccountRequest(12L, GBP.name(), BigDecimal.ONE);
        var body = Mono.just(accountRequest);

        var request = MockServerRequest.builder().body(body);

        accountHandler.createAccount(request).subscribe(response -> assertTrue(response.statusCode().is2xxSuccessful()));

        verify(accountRepository).save(accountCaptor.capture());

        assertEquals(12L, accountCaptor.getValue().getCustomerId());
        assertEquals(GBP, accountCaptor.getValue().getCurrency());
        assertEquals(1, accountCaptor.getValue().getBalance().intValue());
    }

    @Test
    public void shouldDeposit() {
        var usdAccount = new Account(USD, 12L, Instant.now());
        usdAccount.setBalance(BigDecimal.TEN);

        var amount = BigDecimal.ONE;
        var body = Mono.just(new OperationRequest(amount));

        var request = MockServerRequest.builder()
                .pathVariable("accountId", "12")
                .body(body);

        when(accountRepository.findById(12L)).thenReturn(Mono.just(usdAccount));

        Account updatedAccount = new Account(USD, 12L, Instant.now());
        updatedAccount.setBalance(BigDecimal.valueOf(11));
        when(balanceService.addAmountToBalance(usdAccount, amount)).thenReturn(Mono.just(updatedAccount));

        accountHandler.deposit(request).subscribe(response -> assertTrue(response.statusCode().is2xxSuccessful()));

        verify(accountRepository).save(accountCaptor.capture());

        assertEquals(12L, accountCaptor.getValue().getCustomerId());
        assertEquals(USD, accountCaptor.getValue().getCurrency());
        assertEquals(11, accountCaptor.getValue().getBalance().intValue());
    }

    @Test
    public void shouldWithdraw() {
        var usdAccount = new Account(USD, 12L, Instant.now());
        usdAccount.setBalance(BigDecimal.TEN);

        var amount = BigDecimal.ONE;
        var body = Mono.just(new OperationRequest(amount));

        var request = MockServerRequest.builder()
                .pathVariable("accountId", "12")
                .body(body);

        when(accountRepository.findById(12L)).thenReturn(Mono.just(usdAccount));

        Account updatedAccount = new Account(USD, 12L, Instant.now());
        updatedAccount.setBalance(BigDecimal.valueOf(9));
        when(balanceService.subtractAmountFromBalance(usdAccount, amount)).thenReturn(Mono.just(updatedAccount));

        accountHandler.withdraw(request).subscribe(response -> assertTrue(response.statusCode().is2xxSuccessful()));

        verify(accountRepository).save(accountCaptor.capture());

        assertEquals(12L, accountCaptor.getValue().getCustomerId());
        assertEquals(USD, accountCaptor.getValue().getCurrency());
        assertEquals(9, accountCaptor.getValue().getBalance().intValue());
    }

    @Test
    public void shouldTransfer() {
        var amount = BigDecimal.ONE;
        var body = Mono.just(new OperationRequest(amount));

        var request = MockServerRequest.builder()
                .pathVariable("accountIdFrom", "12")
                .pathVariable("accountIdTo", "13")
                .body(body);

        var account1 = new Account(EUR, 12L, Instant.now());
        var account2 = new Account(EUR, 13L, Instant.now());

        when(accountRepository.findById(12L)).thenReturn(Mono.just(account1));
        when(accountRepository.findById(13L)).thenReturn(Mono.just(account2));
        when(balanceService.modifyBalances(any(), eq(amount))).thenReturn(Mono.just(Tuples.of(account1, account2)));

        accountHandler.transfer(request).subscribe(response -> assertTrue(response.statusCode().is2xxSuccessful()));

        verify(accountRepository).saveAll(anySet());
    }

}
