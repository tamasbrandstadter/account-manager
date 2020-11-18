package com.bank.accountmanager.it;

import com.bank.accountmanager.TestConfig;
import com.bank.accountmanager.handler.request.AccountRequest;
import com.bank.accountmanager.handler.request.CustomerRequest;
import com.bank.accountmanager.handler.request.OperationRequest;
import com.bank.accountmanager.repository.AccountRepository;
import com.bank.accountmanager.repository.CustomerRepository;
import com.bank.accountmanager.router.AccountManagerRouter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Scanner;

import static com.bank.accountmanager.repository.model.Currency.USD;
import static java.math.BigDecimal.TEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@ActiveProfiles("it")
@SpringBootTest
@AutoConfigureWebTestClient(timeout = "36000")
@Import(TestConfig.class)
@TestMethodOrder(OrderAnnotation.class)
public class AccountManagerIntegrationTest {
    private static final String CREATE_CUSTOMER_ENDPOINT = "/customer";
    private static final String CREATE_ACCOUNT_ENDPOINT = "/account";
    private static final String GET_BALANCE_ENDPOINT = "/account/{accountId}";
    private static final String DEPOSIT_ENDPOINT = "/account/{accountId}/deposit";
    private static final String WITHDRAW_ENDPOINT = "/account/{accountId}/withdraw";
    private static final String TRANSFER_BETWEEN_ACCOUNTS_ENDPOINT = "/transfer/{accountIdFrom}/{accountIdTo}";

    @Autowired
    private AccountManagerRouter accountManagerRouter;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    public void init() {
        webTestClient = WebTestClient.bindToRouterFunction(accountManagerRouter.routes()).build();
    }

    @Test
    @Order(1)
    public void shouldCreateCustomer() {
        // given

        // when
        webTestClient.post()
                .uri(CREATE_CUSTOMER_ENDPOINT)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(Mono.just(new CustomerRequest("Fancy", "Customer")), CustomerRequest.class)
                .exchange()

                // then
                .expectStatus().isCreated()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody().json(readFile(getClass().getResourceAsStream("/response_bodies/create_customer.json")));

        // and
        customerRepository.findById(3L).subscribe(customer -> assertEquals("Fancy", customer.getFirstName()));
    }

    @Test
    @Order(2)
    public void shouldReturnConflictForDuplicateCustomerCreation() {
        // given

        // when
        webTestClient.post()
                .uri(CREATE_CUSTOMER_ENDPOINT)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(Mono.just(new CustomerRequest("Fancy", "Customer")), CustomerRequest.class)
                .exchange()

                // then
                .expectStatus().isEqualTo(CONFLICT);
    }

    @Test
    @Order(3)
    public void shouldGetBalance() {
        // given

        // when
        webTestClient.get()
                .uri(GET_BALANCE_ENDPOINT, "11")
                .accept(APPLICATION_JSON)
                .exchange()

                // then
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody().json(readFile(getClass().getResourceAsStream("/response_bodies/get_account_balance.json")));

        // and
        accountRepository.findById(11L).subscribe(account -> assertEquals(Instant.parse("2020-11-15T13:00:00Z"), account.getCreatedAt()));
    }

    @Test
    @Order(4)
    public void shouldReturnNotFoundForAccountBalance() {
        // given

        // when
        webTestClient.get()
                .uri(GET_BALANCE_ENDPOINT, "12453")
                .accept(APPLICATION_JSON)
                .exchange()

                // then
                .expectStatus().isNotFound();
    }

    @Test
    @Order(5)
    public void shouldCreateAccount() {
        // given

        // when
        webTestClient.post()
                .uri(CREATE_ACCOUNT_ENDPOINT)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(Mono.just(new AccountRequest(3L, USD.name(), TEN)), AccountRequest.class)
                .exchange()

                // then
                .expectStatus().isCreated()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody().json(readFile(getClass().getResourceAsStream("/response_bodies/create_account.json")));

        // and
        accountRepository.findById(23L).subscribe(account -> {
            assertEquals(10, account.getBalance().intValue());
            assertNotNull(account.getCreatedAt());
        });
    }

    @Test
    @Order(6)
    public void shouldReturnNotFoundIfCustomerIsMissing() {
        // given

        // when
        webTestClient.post()
                .uri(CREATE_ACCOUNT_ENDPOINT)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(Mono.just(new AccountRequest(868L, USD.name(), TEN)), AccountRequest.class)
                .exchange()

                // then
                .expectStatus().isNotFound();
    }

    @Test
    @Order(7)
    public void shouldDeposit() {
        // given

        // when
        webTestClient.put()
                .uri(DEPOSIT_ENDPOINT, "23")
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(Mono.just(new OperationRequest(BigDecimal.valueOf(100))), OperationRequest.class)
                .exchange()

                // then
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody().json(readFile(getClass().getResourceAsStream("/response_bodies/deposit.json")));

        // and
        accountRepository.findById(23L).subscribe(account -> assertEquals(110, account.getBalance().intValue()));
    }

    @Test
    @Order(8)
    public void shouldReturnBadRequestForNegativeDepositAmount() {
        // given

        // when
        webTestClient.put()
                .uri(DEPOSIT_ENDPOINT, "23")
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(Mono.just(new OperationRequest(BigDecimal.valueOf(-1))), OperationRequest.class)
                .exchange()

                // then
                .expectStatus().isBadRequest()
                .expectBody(String.class).isEqualTo("Deposit amount must be greater than 0");

        // and
        accountRepository.findById(23L).subscribe(account -> assertEquals(110, account.getBalance().intValue()));
    }

    @Test
    @Order(9)
    public void shouldReturnBadRequestForZeroDeposit() {
        // given

        // when
        webTestClient.put()
                .uri(DEPOSIT_ENDPOINT, "23")
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(Mono.just(new OperationRequest(BigDecimal.ZERO)), OperationRequest.class)
                .exchange()

                // then
                .expectStatus().isBadRequest()
                .expectBody(String.class).isEqualTo("Deposit amount must be greater than 0");

        // and
        accountRepository.findById(23L).subscribe(account -> assertEquals(110, account.getBalance().intValue()));
    }

    @Test
    @Order(10)
    public void shouldReturnNotFoundIfAccountIsMissingForDeposit() {
        // given

        // when
        webTestClient.put()
                .uri(DEPOSIT_ENDPOINT, "233434")
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(Mono.just(new OperationRequest(BigDecimal.ONE)), OperationRequest.class)
                .exchange()

                // then
                .expectStatus().isNotFound();
    }

    @Test
    @Order(11)
    public void shouldWithdraw() {
        // given

        // when
        webTestClient.put()
                .uri(WITHDRAW_ENDPOINT, "23")
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(Mono.just(new OperationRequest(BigDecimal.valueOf(5))), OperationRequest.class)
                .exchange()

                // then
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody().json(readFile(getClass().getResourceAsStream("/response_bodies/withdraw.json")));

        // and
        accountRepository.findById(23L).subscribe(account -> assertEquals(105, account.getBalance().intValue()));
    }

    @Test
    @Order(12)
    public void shouldReturnBadRequestForNegativeWithdrawAmount() {
        // given

        // when
        webTestClient.put()
                .uri(WITHDRAW_ENDPOINT, "23")
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(Mono.just(new OperationRequest(BigDecimal.valueOf(-5))), OperationRequest.class)
                .exchange()

                // then
                .expectStatus().isBadRequest()
                .expectBody(String.class).isEqualTo("Withdraw amount must be greater than 0");

        // and
        accountRepository.findById(23L).subscribe(account -> assertEquals(105, account.getBalance().intValue()));
    }

    @Test
    @Order(13)
    public void shouldReturnBadRequestForZeroWithdrawAmount() {
        // given

        // when
        webTestClient.put()
                .uri(WITHDRAW_ENDPOINT, "23")
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(Mono.just(new OperationRequest(BigDecimal.ZERO)), OperationRequest.class)
                .exchange()

                // then
                .expectStatus().isBadRequest()
                .expectBody(String.class).isEqualTo("Withdraw amount must be greater than 0");

        // and
        accountRepository.findById(23L).subscribe(account -> assertEquals(105, account.getBalance().intValue()));
    }

    @Test
    @Order(14)
    public void shouldReturnNotFoundIfAccountIsMissingForWithdraw() {
        // given

        // when
        webTestClient.put()
                .uri(WITHDRAW_ENDPOINT, "233434")
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(Mono.just(new OperationRequest(BigDecimal.ONE)), OperationRequest.class)
                .exchange()

                // then
                .expectStatus().isNotFound();
    }

    @Test
    @Order(15)
    public void shouldTransfer() {
        // given

        // when
        webTestClient.put()
                .uri(TRANSFER_BETWEEN_ACCOUNTS_ENDPOINT, "23", "22")
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(Mono.just(new OperationRequest(BigDecimal.valueOf(25.5))), OperationRequest.class)
                .exchange()

                // then
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody().json(readFile(getClass().getResourceAsStream("/response_bodies/transfer.json")));

        // and
        accountRepository.findById(23L).subscribe(account -> assertEquals(79.5, account.getBalance().doubleValue()));
        accountRepository.findById(22L).subscribe(account -> assertEquals(25.5, account.getBalance().doubleValue()));
    }

    @Test
    @Order(16)
    public void shouldReturnBadRequestForNegativeTransferAmount() {
        // given

        // when
        webTestClient.put()
                .uri(TRANSFER_BETWEEN_ACCOUNTS_ENDPOINT, "23", "22")
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(Mono.just(new OperationRequest(BigDecimal.valueOf(-1))), OperationRequest.class)
                .exchange()

                // then
                .expectStatus().isBadRequest()
                .expectBody(String.class).isEqualTo("Transfer amount must be greater than 0");

        // and
        accountRepository.findById(23L).subscribe(account -> assertEquals(79.5, account.getBalance().doubleValue()));
        accountRepository.findById(22L).subscribe(account -> assertEquals(25.5, account.getBalance().doubleValue()));
    }

    @Test
    @Order(17)
    public void shouldReturnBadRequestForZeroTransferAmount() {
        // given

        // when
        webTestClient.put()
                .uri(TRANSFER_BETWEEN_ACCOUNTS_ENDPOINT, "23", "22")
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(Mono.just(new OperationRequest(BigDecimal.valueOf(0))), OperationRequest.class)
                .exchange()

                // then
                .expectStatus().isBadRequest()
                .expectBody(String.class).isEqualTo("Transfer amount must be greater than 0");

        // and
        accountRepository.findById(23L).subscribe(account -> assertEquals(79.5, account.getBalance().doubleValue()));
        accountRepository.findById(22L).subscribe(account -> assertEquals(25.5, account.getBalance().doubleValue()));
    }

    @Test
    @Order(18)
    public void shouldReturnNotFoundIfFromAccountIsMissing() {
        // given

        // when
        webTestClient.put()
                .uri(TRANSFER_BETWEEN_ACCOUNTS_ENDPOINT, "43242", "22")
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(Mono.just(new OperationRequest(BigDecimal.valueOf(5))), OperationRequest.class)
                .exchange()

                // then
                .expectStatus().isNotFound();

        // and
        accountRepository.findById(22L).subscribe(account -> assertEquals(25.5, account.getBalance().doubleValue()));
    }

    @Test
    @Order(19)
    public void shouldReturnNotFoundIfToAccountIsMissing() {
        // given

        // when
        webTestClient.put()
                .uri(TRANSFER_BETWEEN_ACCOUNTS_ENDPOINT, "23", "23423523")
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(Mono.just(new OperationRequest(BigDecimal.valueOf(5))), OperationRequest.class)
                .exchange()

                // then
                .expectStatus().isNotFound();

        // and
        accountRepository.findById(23L).subscribe(account -> assertEquals(79.5, account.getBalance().doubleValue()));
    }

    private String readFile(InputStream stream) {
        return new Scanner(stream).useDelimiter("\\A").next();
    }

}
