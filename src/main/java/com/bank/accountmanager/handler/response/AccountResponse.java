package com.bank.accountmanager.handler.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;

public record AccountResponse(@JsonProperty("accountId") Long accountId, @JsonProperty("balance") BigDecimal balance,
                              @JsonProperty("currency") String currency, @JsonProperty("timestamp") Instant timestamp) {
    
}
