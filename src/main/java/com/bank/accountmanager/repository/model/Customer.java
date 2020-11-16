package com.bank.accountmanager.repository.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("customers")
public class Customer {

    @Id
    @Column("customer_id")
    private Long id;

    @Column("first_name")
    private final String firstName;

    @Column("last_name")
    private final String lastName;

}
