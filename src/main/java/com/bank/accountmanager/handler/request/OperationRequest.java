package com.bank.accountmanager.handler.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record OperationRequest(@JsonProperty("amount") BigDecimal amount) {

}
