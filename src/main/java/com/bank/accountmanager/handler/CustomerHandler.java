package com.bank.accountmanager.handler;

import com.bank.accountmanager.handler.request.CustomerRequest;
import com.bank.accountmanager.repository.CustomerRepository;
import com.bank.accountmanager.repository.model.Customer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomerHandler {

    private final CustomerRepository customerRepository;

    public Mono<ServerResponse> createCustomer(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(CustomerRequest.class)
                .filterWhen(request -> customerRepository.notExistByFirstAndLastName(request.firstName(), request.lastName()))
                .map(request -> new Customer(request.firstName(), request.lastName()))
                .flatMap(customerRepository::save)
                .doOnNext(customer -> log.info("Successfully created customer with id {}", customer.getId()))
                .flatMap(savedCustomer -> ServerResponse.created(URI.create("/customers/%s".formatted(savedCustomer.getId())))
                        .contentType(APPLICATION_JSON)
                        .bodyValue(savedCustomer))
                .switchIfEmpty(ServerResponse.status(HttpStatus.CONFLICT).build());
    }

}
