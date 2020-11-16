package com.bank.accountmanager.repository;

import com.bank.accountmanager.repository.model.Customer;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface CustomerRepository extends ReactiveCrudRepository<Customer, Long> {

    @Query("SELECT NOT EXISTS(SELECT 1 from customers where last_name = :lastName AND first_name = :firstName)")
    Mono<Boolean> notExistByFirstAndLastName(String firstName, String lastName);

}
