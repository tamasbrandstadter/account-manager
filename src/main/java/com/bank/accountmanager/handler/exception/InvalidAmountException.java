package com.bank.accountmanager.handler.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor
@Data
public class InvalidAmountException extends Exception {

    private final String message;

}
