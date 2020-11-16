package com.bank.accountmanager.handler;

import com.bank.accountmanager.handler.request.CustomerRequest;
import com.bank.accountmanager.repository.CustomerRepository;
import com.bank.accountmanager.repository.model.Customer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomerHandlerTest {

    @Mock(answer = RETURNS_DEEP_STUBS)
    private CustomerRepository customerRepository;

    @Captor
    private ArgumentCaptor<Customer> customerCaptor;

    @InjectMocks
    private CustomerHandler customerHandler;

    @Test
    public void shouldCreateCustomer() {
        var customerRequest = new CustomerRequest("first", "last");
        var body = Mono.just(customerRequest);

        when(customerRepository.notExistByFirstAndLastName("first", "last")).thenReturn(Mono.just(true));

        var request = MockServerRequest.builder().body(body);

        customerHandler.createCustomer(request).subscribe();

        verify(customerRepository).save(customerCaptor.capture());

        assertEquals(customerRequest.firstName(), customerCaptor.getValue().getFirstName());
        assertEquals(customerRequest.lastName(), customerCaptor.getValue().getLastName());
    }

}
