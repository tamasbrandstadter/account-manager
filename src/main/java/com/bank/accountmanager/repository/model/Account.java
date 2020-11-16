package com.bank.accountmanager.repository.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

@Data
@Table("accounts")
public class Account {

    @Id
    @Column("account_id")
    private Long id;

    private BigDecimal balance = BigDecimal.ZERO;

    private final Currency currency;

    @Column("customer_id")
    private final Long customerId;

    @Column("created_at")
    @JsonFormat(shape = STRING)
    private final Instant createdAt;

}
