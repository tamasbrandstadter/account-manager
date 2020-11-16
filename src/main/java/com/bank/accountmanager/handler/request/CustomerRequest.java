package com.bank.accountmanager.handler.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CustomerRequest(@JsonProperty("firstName") String firstName, @JsonProperty("lastName") String lastName) {

}
