package com.bank.accountmanager.router;

import com.bank.accountmanager.handler.AccountHandler;
import com.bank.accountmanager.handler.CustomerHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Configuration
@RequiredArgsConstructor
public class AccountManagerRouter {
    private static final String CREATE_CUSTOMER_ENDPOINT = "/customer";
    private static final String CREATE_ACCOUNT_ENDPOINT = "/account";
    private static final String DEPOSIT_ENDPOINT = "/account/{accountId}/deposit";
    private static final String WITHDRAW_ENDPOINT = "/account/{accountId}/withdraw";
    private static final String GET_BALANCE_ENDPOINT = "/account/{accountId}";
    private static final String TRANSFER_BETWEEN_ACCOUNTS_ENDPOINT = "/transfer/{accountIdFrom}/{accountIdTo}";

    private final CustomerHandler customerHandler;
    private final AccountHandler accountHandler;

    @Bean
    public RouterFunction<ServerResponse> routes() {
        return RouterFunctions.route()
                .GET(GET_BALANCE_ENDPOINT, accountHandler::getAccountBalance)
                .POST(CREATE_CUSTOMER_ENDPOINT, RequestPredicates.contentType(APPLICATION_JSON), customerHandler::createCustomer)
                .POST(CREATE_ACCOUNT_ENDPOINT, RequestPredicates.contentType(APPLICATION_JSON), accountHandler::createAccount)
                .PUT(DEPOSIT_ENDPOINT, RequestPredicates.contentType(APPLICATION_JSON), accountHandler::deposit)
                .PUT(WITHDRAW_ENDPOINT, RequestPredicates.contentType(APPLICATION_JSON), accountHandler::withdraw)
                .PUT(TRANSFER_BETWEEN_ACCOUNTS_ENDPOINT, RequestPredicates.contentType(APPLICATION_JSON), accountHandler::transfer)
                .build();
    }

}
