package com.bank.accountmanager.handler.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record AccountRequest(@JsonProperty("customerId") Long customerId, @JsonProperty("currency") String currency,
                             @JsonProperty("initialDeposit") BigDecimal initialDeposit) {

}
